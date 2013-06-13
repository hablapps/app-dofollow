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

package org.hablapps.dofollow.test
import org.hablapps.{ updatable, react, speech }
import updatable._
import org.hablapps.speech.serializer._
import org.hablapps.serializer.{serializeMe}

import org.hablapps.dofollow
import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.administration._
import org.hablapps.dofollow.portal.administration.projectModel._
import org.hablapps.dofollow.portal.project._
import org.hablapps.dofollow.portal.project.task._

object ConsoleDoFollow extends App {

	object ConsoleSystem extends DoFollowSystem with speech.web.PlainSystem{
		
    	serializeMe[this.type]

    	applicationFilesPath = "static/"

		reset(for {
		  	portal1 			<- Initiate(Portal(_name = Some("portal")))
		    administration 		<- Initiate2(Administration(_name = Some("administration")), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("admin")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    dep2 				<- Initiate2(Department(_departmentName = "Admins", _name = Some("37")), portal1)
		    modelPro1 			<- Initiate2(ProjectModel(_projectName = "ProjectType 1", _name = Some("1")), administration)
		    modelTask1 			<- Initiate2(TaskModel(_name = Some("t1"), _taskName = "Task 1", 
					    			_description = "Description",
					    			_dependencies = Set(),
					    			_duration = 3,
					    			_department = "dep1",
					    			_waitingDate = None,
					    			_temporalDependence = None, 
					    			_temporalDuration = None), modelPro1)
		    catTask2 			<- Initiate2(TaskModel(_name = Some("t2"), _taskName = "Task 2", 
					    			_description = "description 2",
					    			_dependencies = Set("t1"),
					    			_duration = 3,
					    			_department = "dep1",
					    			_waitingDate = None,
					    			_temporalDependence = None, 
					    			_temporalDuration = None), modelPro1)
		} yield ())

		turn_on_log = true
	}

	ConsoleSystem.launch

}