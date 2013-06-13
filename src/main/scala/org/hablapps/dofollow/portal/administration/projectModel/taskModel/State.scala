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

package org.hablapps.dofollow.portal.administration.projectModel.taskModel

import org.hablapps.updatable._
import org.hablapps.react._
import org.hablapps.speech

import org.hablapps.dofollow.portal.administration._

trait State{ self: speech.Program 
	with projectModel.State =>

	/**
	*
	*	A kind of task.
	*	It has all the information of this kind of task.
	*
	*/
	
	trait TaskModel extends Interaction {
		type This = TaskModel
		type Substatus = Nothing
		type ContextCol[x] = Option[x]

		/**		The context is a Project model	*/

		type Context = ProjectModel
		type SubinteractionCol[x] = Traversable[x]
		type Subinteraction = Nothing
		type MemberCol[x] = Traversable[x]
		type Member = Nothing
		type EnvironmentCol[x] = Traversable[x]
		type Environment = Nothing
		type ActionCol[x] = Traversable[x]
		type Action = Nothing

		/** Project's name
		*
		* @kind input
		*
		*/

		val taskName:String

		/** Description of the task
		*
		* @kind input
		*
		*/		

		val description:String

		/** Tasks that depend on this task
		*
		* @kind input
		*
		*/

		val dependencies:Set[String]

		/** Duration of the task
		*
		* @kind input
		*
		*/

		val duration:Int

		/** Department which the task belongs.
		*
		* @kind input
		*
		*/

		val department:String

		/** Date when the task is initiated
		*
		* @kind local
		*
		*/		

		val waitingDate: Option[Long]

		/** Task that depend on this task (temporarily)
		*
		* @kind input
		*
		*/

		val temporalDependence: Option[String]

		/** Waiting time for launch.
		*
		* @kind input
		*
		*/

		val temporalDuration: Option[Int]
	}
	
	implicit val TaskModel = builder[TaskModel]
	

} 
