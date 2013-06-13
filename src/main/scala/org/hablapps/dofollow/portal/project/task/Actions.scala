/*
 * Copyright (c) 2013 Habla Computing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.hablapps.dofollow.portal.project.task

import org.hablapps.updatable._
import org.hablapps.speech

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.project._
import org.hablapps.dofollow.portal.project.task._
import org.hablapps.dofollow.portal.project.task.discussion._

trait Actions { this: speech.Program 
	with State
	with department.State
	with discussion.State
	with portal.State
	with project.State
	with administration.State  =>

	/**		Checks dependencies of a task	*/

	case class CheckNextDependencies(task : $[Task]) extends DefinedAction( implicit state =>
		For(task.childs) {
			case t if t.normalDependencies && t.temporalDependencies && t.substatus.get == Waiting =>
					Let(t, "substatus", Executing, true)
			case _ => ActionId()
			
		}
	)

	/**		Checks temporal dependencies of a task	*/

	case class CheckTemporalDependencies(t: $[Task]) extends DefinedAction( implicit state =>
		For(t.project.tasks){
			case tar if tar.temporalDependence == Some(t) && tar.substatus.get == Waiting =>
					Let(tar, "waitingDate", addBusinessDays(now, tar.temporalDuration.get), true)
			case _ => ActionId()
		}
	)

	/**		Calculates deadlines of tasks in cache	*/

	case class CalculateDeadlines(filterTasks: Set[$[Task]]) extends DefinedAction( implicit state =>
		if (!filterTasks.isEmpty)
			Sequence(
				For(filterTasks){
					case t => LetWholeExtension(t, "deadline", None)
				},
				RecursiveDates(filterTasks)
			)
		else
			ActionId()
	)

	case class RecursiveDates(filterTasks: Set[$[Task]]) extends DefinedAction[Any]( implicit state =>
		if(filterTasks.isEmpty)
			ActionId()
		else{
			val filterTask = filterTasks.filter(t => t.contextDependenciesChecks && t.temporalDependenciesChecks)
			if (filterTask.isEmpty)
				ActionId()
			else
				Sequence(
					implicit state => LetDeadline(filterTask.head),
					implicit state => RecursiveDates(filterTasks - filterTask.head)
				)
		}
	)

	case class LetDeadline(t: $[Task]) extends DefinedAction( implicit state =>
		Sequence(
			{if(t.temporalDependence.isDefined)
				if(!t.waitingDate.isDefined)
					if(t.temporalDependence.get.substatus == Some(Finished) || t.temporalDependence.get.substatus == Some(Cancelled))
						Let(t, "deadline", addBusinessDays(t.temporalDependence.get.endDate, t.duration + t.temporalDuration.get - t.temporalDependence.get.duration), true)
					else
						Let(t, "deadline", addBusinessDays(t.temporalDependence.get.deadline.get, t.duration + t.temporalDuration.get - t.temporalDependence.get.duration), true)
					
				else
					Let(t, "deadline", addBusinessDays(t.waitingDate.get, t.duration), true)
			else
				ActionId()},
			{if(t.predecessor.isEmpty)
				ActionId()
			else
				if(t.predecessor.filter(y => y.substatus != Some(Finished) && y.substatus != Some(Cancelled)).isEmpty)
					Let(t, "deadline", addBusinessDays(t.predecessor.toList.sortBy(- _.deadline.get).head.deadline.get, t.duration + 1), true)
				else
					Let(t, "deadline", addBusinessDays(t.predecessor.filter(y => y.substatus != Some(Finished) && y.substatus != Some(Cancelled)).toList.sortBy(- _.deadline.get).head.deadline.get, t.duration + 1), true)
			}
		)
	)

	/**		Calculates deadlines of tasks in persistence	*/

	type TaskInfo2 = (Long,TaskState,Map[String,Any],Set[$[Task]], Option[Long])

	case class CalculateDeadlinesWithQuery(m: Map[$[Task], TaskInfo2], t: $[Task]) extends DefinedAction( implicit state =>
		if(m.isEmpty)
			ActionId()
		else{
			val newMap = RecursiveDatesWithMap(m, getChildless(m, t))
			For(newMap.toList){
				case (k, (oldDate, substatus, _, _, newDate)) if substatus == Waiting && newDate.isDefined && (newDate.get != oldDate) => 
					Let(k, "deadline", newDate.get, true) 
				case _ => ActionId()
			}
		}	
	)

	def getDeadline(m: Map[$[Task], TaskInfo2], t: $[Task]) = 
		m(t)._5.getOrElse(m(t)._1)

	def getChildless(m: Map[$[Task], TaskInfo2], t: $[Task]) : Set[$[Task]] = {
		m.filter{case(k,(_,_,atts,dependsOn,_)) =>  dependsOn.contains(t) || (atts.get("temporalDependence") match{
			case Some(dt1: $[Task]) => dt1 == t
			case _ => false
			})}.map{case (k,v) => k}.toSet
	}

	def contextParentsResolved(m: Map[$[Task], TaskInfo2], t: $[Task]) = 
		if(m(t)._4.isEmpty)
			true
		else
			m(t)._4.forall(dependent => isResolved(m, dependent))

	def temporalParentsResolved(m: Map[$[Task], TaskInfo2], t: $[Task]) = 
		m(t)._3.get("temporalDependence") match{
			case Some(task: $[Task]) => isResolved(m, task)
			case _ => true
		}

	def isResolved(m: Map[$[Task], TaskInfo2], t: $[Task]) = 
		m(t)._2 != Waiting || m(t)._5.isDefined

	def RecursiveDatesWithMap(m: Map[$[Task], TaskInfo2], electedTasks: Set[$[Task]]): Map[$[Task], TaskInfo2] = {

		val filteredTasks = electedTasks.filter(t => contextParentsResolved(m, t) && temporalParentsResolved(m, t) && !isResolved(m, t))

		if (filteredTasks.isEmpty)
			m
		else
			RecursiveDatesWithMap(LetDeadlineWithMap(m, filteredTasks.head), electedTasks - filteredTasks.head ++ getChildless(m, filteredTasks.head))
	}

	def LetDeadlineWithMap(m: Map[$[Task], TaskInfo2], t: $[Task]): Map[$[Task], TaskInfo2] = {
		if(!m(t)._4.isEmpty){  					
			if(m(t)._4.filter(y => m(y)._2 != Finished && m(y)._2 != Cancelled).isEmpty){
				m + ((t , (m(t)._1,
							m(t)._2,
							m(t)._3,		
							m(t)._4,
							Some(addBusinessDays(m(t)._4.toList.map{
								case dependent => m(dependent)._5.getOrElse(m(dependent)._1) }
								.sortWith(_ > _).head, m(t)._3("duration").asInstanceOf[Int] + 1)))))
			}else{
				m + ((t , (m(t)._1,
							m(t)._2,
							m(t)._3,		
							m(t)._4,
							Some(addBusinessDays(m(t)._4.toList.map{
									case dependent if m(dependent)._2 != Finished && m(dependent)._2 != Cancelled => getDeadline(m, dependent) 
									case _ => 0.toLong
							}.sortWith(_ > _).head, m(t)._3("duration").asInstanceOf[Int] + 1)))))
			}
		}else{
			m(t)._3.get("temporalDependence") match {
				case Some(task: $[Task]) =>	
					m + ((t , (m(t)._1,
							m(t)._2,
							m(t)._3,		
							m(t)._4,
							Some(addBusinessDays(getDeadline(m, m(t)._3("temporalDependence").asInstanceOf[$[Task]]), 
															m(t)._3("duration").asInstanceOf[Int] + 
															m(t)._3("temporalDuration").asInstanceOf[Int] 
															- m(m(t)._3("temporalDependence").asInstanceOf[$[Task]])._3("duration").asInstanceOf[Int])))))
				case _ => m					
			}
		}
	}

}