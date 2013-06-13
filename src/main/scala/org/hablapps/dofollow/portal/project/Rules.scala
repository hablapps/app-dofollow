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

package org.hablapps.dofollow.portal.project

import org.hablapps.updatable._
import org.hablapps.speech

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._

trait Rules{ this: speech.Program
	with State
	with meeting.State
	with portal.State
	with project.State
	with task.State
	with department.State
	with administration.State =>

	/**		Links a task with his department 	*/

	when {
	    case Performed(setUpTask1: SetUpTask) if setUpTask1.isA[SetUpTask] => implicit state =>
	    	Sequence(
	    		Let(setUpTask1.project.portal.departments.filter(_.name.get == setUpTask1.department).head.subinteraction += setUpTask1._new_entity.get),
	    		Let(setUpTask1._new_entity.get.context += setUpTask1.project.portal.departments.filter(_.name.get == setUpTask1.department).head)  		
	    	)
	  }
	
    /**		Creates a Moderator when a new meeting is initiated	
    *
    *		@play Moderator	When a new meeting is initiated
    *
    */

	when {
	    case Performed(setUp: SetUpMeeting)  if setUp.isA[SetUpMeeting] => implicit state =>
			Play3(Moderator(), setUp.performer.get, setUp._new_entity.get) 	
	}

	/**		Creates the start date when a new project is initiated	*/

	when {
	    case New(p: $[Project], _: Project) => implicit state =>
	    	LetWholeExtension(p, "startDate", now)
	  }

    /**		When a project is closed, it's necessary to close the tasks and meetings	
    *
    *		@finish Task 		When the project of the task is closed
    *		@finish Meeting 	When the project of the meeting is closed
    *
    */

    when {
        case _Set(p: $[Project], Project._status, CLOSED, true) if p.isA[Project]=> implicit state =>
        	For(p.subinteraction){
        		case task: $[Task] if task.isA[Task] && task.substatus.get != Cancelled && task.substatus.get != Finished => 
        			Sequence(
        				Let(task, "substatus", Cancelled, true),
        				LetWholeExtension(task, "endDate", now),
        				Finish(task)
    				)
        			
        		case meeting: $[Meeting] if meeting.isA[Meeting] && meeting.substatus.get != Undoned && meeting.substatus.get != Completed => 
        			Sequence(
        				Let(meeting, "substatus", Undoned, true),
        				Finish(meeting)
    				)
    			case _ => ActionId()
        	}
      }
    
	

} 