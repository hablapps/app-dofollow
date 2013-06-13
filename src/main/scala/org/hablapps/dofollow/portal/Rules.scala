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

package org.hablapps.dofollow.portal

import org.hablapps.updatable._
import org.hablapps.speech

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.administration._
import org.hablapps.dofollow.portal.department._
import org.hablapps.dofollow.portal.project._
import org.hablapps.dofollow.portal.project.task._

trait Rules{ this: speech.Program
	with State
	with discussion.State
	with meeting.State
	with projectModel.State
	with department.State
	with administration.State
	with project.State
	with task.State
	with Actions =>

	/** Deploy Portal: initiate Administration, played Admin and charge tasks and projects models.
	*
	*	@initiate 	Administration 		Administration is initiated when the portal is created.
	*	@play 		Admin				Admin is played when the portal is created.
	*
	*/

	when {
	    case New(portal1: $[Portal]@unchecked, _: Portal) if portal1.isA[Portal]=>
	    	Sequence(
		    	for {
			    	admin1				<- Initiate2(Administration(), portal1)
			    	_ 					<- Play2(Admin(_name = Some(adminCod), _email = adminEmail, _forename = forename,
			    								_surname = surname), admin1)
			    	_ 				    <- LoadDepartments(portal1)
			    	_                   <- LoadProjectModels(admin1)
		    	} yield()
		    )
	}

	/*
	*
	*	When a new proyect is created it's necessary to initialize the attributes.
	*	Later, if the adminCode doesn't exist, a new admin will be created.
	*	Finally, tasks will be initiated.
	*
	*/

	when {
	    case Performed(setUpProject1: SetUpProject) => implicit state =>
	    	Sequence(
	    		Let(setUpProject1._new_entity.get.name += setUpProject1.codProject),
	    		Let(setUpProject1._new_entity.get.projectAdmin := setUpProject1.numProjectAdmin),
	    		implicit state => if (!setUpProject1.operator.isDefined)
	    			PlayProjectAdmin(setUpProject1.numProjectAdmin, setUpProject1.getAdminDepartment)
    			else
    				ActionId()
				,
	    		InitiateTasks(setUpProject1)
	    	)
	}

	/** 	Updates the end date when a project is closed	*/

	when {
	    case _Set(project: $[Project], Project._status, CLOSED, true) if project.isA[Project] => implicit state =>
	    	LetWholeExtension(project, "endDate", now)
	  }
	

	/*	Rules for persistent entities	*/

	when {
	    case New(i: $[Interaction]@unchecked, _: Interaction) =>
	    	LetWholeExtension(i, "persistent", true)
	  }

	when {
	    case New(a: $[Agent]@unchecked, _: Agent) =>
	    	LetWholeExtension(a, "persistent", true)
	  }

	when {
	    case New(r: $[Resource]@unchecked, _: Resource) =>
	    	LetWholeExtension(r, "persistent", true)
	  }

	when {
	    case New(r: $[Reply]@unchecked, _: Reply) =>
	    	LetWholeExtension(r, "persistent", true)
	  }

	when {
	    case New(c: $[Comment]@unchecked, _: Comment) =>
	    	LetWholeExtension(c, "persistent", true)
	  }

	when {
	    case New(p: $[ChangeDateMeeting]@unchecked, _: ChangeDateMeeting) =>
	    	LetWholeExtension(p, "persistent", true)
	  }

	when {
	    case New(p: $[ScheduleTask]@unchecked, _: ScheduleTask) =>
	    	LetWholeExtension(p, "persistent", true)
	  }

}
