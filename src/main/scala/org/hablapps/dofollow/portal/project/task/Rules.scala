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

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

import org.hablapps.updatable._
import org.hablapps.speech

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.project._
import org.hablapps.dofollow.portal.project.task._

trait Rules{ this: speech.Program
	with State
	with department.State
	with administration.State
	with portal.State
	with project.State
	with discussion.State
    with meeting.State
	with Actions =>


    /**    Updates the substatus of a task to pending to executed     */

    when {
        case Performed(setUp: SetUpTask) => implicit state =>
        		Let(setUp._new_entity.head, "substatus", Waiting, true)
      }

    /**     When a new responsible is assigned, the old responsible is abandoned     
    *
    *       @abandon    Responsible     When there is a new responsible in the task
    *
    */

	when {
		case Performed(assign:AssignResponsible) if assign.performer.head.isA[Responsible] => implicit state =>
			assign.performer.head match {
				case x: $[Responsible] if x.isA[Responsible] => Abandon(x)
			}
	}

    /**    When a task is executed it's necessary to update some attributes    
    *
    *       @play Responsible   When there aren't operator in the department, the Admin will be the responsible of the task
    *       @play Responsible   When the department of the task is the admin department, the project admin will be the responsible of the task
    *
    */
      
    when {
        case _Set(t: $[Task], SocialEntity._substatus, Executing, true) if t.isA[Task] => implicit state =>
            Sequence(
                if(t.department.name.get != adminDepartment.toString) {
                    if(t.department.member.filter(_.status.get == PLAYING).isEmpty){
                        Play3(Responsible(),   t.project.portal.administration.admin,  t)
                    } else {
                        ActionId()
                    }
                } else {
                    val projectAdmin = t.project.portal.departments.filter(_.name.get == adminDepartment.toString).head.
                            operators.filter(_.name.get == t.project.projectAdmin).head
                    if(projectAdmin.status.head == PLAYING)
                        Play3(Responsible(), projectAdmin,t)
                    else
                        ActionId()
                },
                Sequence(
                    LetWholeExtension(t, "startDate" , now),
                    {
                    if(t.deadline != Some(addBusinessDays(now, t.duration)))
                        Sequence(
                            Let(t, "deadline" , addBusinessDays(now, t.duration), true),
                            CalculateDeadlines(t.project.tasks.filter(tAux => tAux.substatus == Some(Waiting) && tAux.launchType == "A"))
                        )
                    else
                        ActionId()
                    },
                    CheckTemporalDependencies(t)
                )
            )
        }

    when {
        case Performed(schedule: ScheduleTask) if schedule.isA[ScheduleTask] => implicit state => {
            CalculateDeadlines(schedule.task.project.tasks.filter(t => t.substatus == Some(Waiting) && t.launchType == "A"))
        }
      }
    

    /**    When a task is closed, it's necessary to close discussions, abandon the responsible and destroy annexes   
    *
    *       @abandon    Responsible     When the task is finished
    *       @delete     Annex           When the task is finished
    *       @finish     Discussion      When the task is finished
    *
    */

	when {
    	case _Set(task: $[Task], SocialEntity._substatus, Finished, true) if task.isA[Task]  => implicit state =>
      		Sequence(
    			For(task.member){
    				case resp: $[Responsible] if resp.isA[Responsible] && resp.status.head == PLAYING =>
						Sequence(
    						Let(resp, "substatus", ZombieTask, true),
    						Abandon(resp)
						)
					case _ => ActionId()
    			},
    			For(task.environment){
    				case r: $[Annex] if r.status.head == CREATED => Sequence(
    					Let(r, "substatus", ZombieTask, true),
    					DeleteR(r)
					)
    				case _ => ActionId()
    			},
    			For(task.subinteraction){
    				case m: $[Discussion] if m.status.head == OPEN  && m.isA[Discussion] => Sequence(
    					Let(m, "substatus", ZombieTask, true),
    					Finish(m)
					)
    				case _ => ActionId()
    			}
	    	)
   	}

    /**    When a task is cancelled, it's necessary to close discussions, abandon the responsible and destroy annexes   
    *
    *       @abandon    Responsible     When the task is finished
    *       @delete     Annex           When the task is finished
    *       @finish     Discussion      When the task is finished
    *    
    */

   	when {
    	case _Set(task: $[Task], SocialEntity._substatus, Cancelled, true) if task.isA[Task] => implicit state =>
      		Sequence(
    			For(task.member){
    				case resp: $[Responsible] if resp.isA[Responsible] && resp.status.head == PLAYING =>
						Abandon(resp)
					case _ => ActionId()
    			},
    			For(task.environment){
    				case r: $[Annex] if r.status.head == CREATED => DeleteR(r)
    				case _ => ActionId()
    			},
    			For(task.subinteraction){
    				case m: $[Discussion] if m.status.head == OPEN  && m.isA[Discussion] => Finish(m)
    				case _ => ActionId()
    			}
	    	)
   	}

    /**    When a new annex is created it's necessary to update the upload date and the creator    */

    when {
        case Performed(create: CreateAnnex) => implicit state =>
            Sequence(
                Let(create._new_entity.head, "creator", create.creator, true),
                LetWholeExtension(create._new_entity.head, "uploadDate", now)
            )
            
      }

    /**    When a responsible is abandoned it's necessary to abandon his roles  
    *
    *       @abandon Issuer     When his player is abandoned
    *       @abandon Receiver   When his player is abandoned
    *
    */

    when {
        case _Set(resp: $[Responsible]@unchecked, Responsible._status, ABANDONED, true) if resp.isA[Responsible] =>
            For(resp.role){
                case rol: $[Issuer] if rol.status.get == PLAYING && rol.isA[Issuer] => Abandon(rol)
                case rol: $[Receiver] if rol.status.get == PLAYING && rol.isA[Receiver] => Abandon(rol)
                case _ => ActionId()
            }
    }

    /**    When a new comment is created it's necessary to update the date  */

    when {
        case New(comment: $[Comment], _: Comment) => implicit state =>
            LetWholeExtension(comment, "date" , nowWithMillis)
      }
}