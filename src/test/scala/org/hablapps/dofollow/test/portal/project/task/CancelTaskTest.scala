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

package org.hablapps.dofollow.test.portal.project.task

import org.scalatest.FunSpec
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.ShouldMatchers

import org.hablapps.updatable._
import org.hablapps.react
import org.hablapps.speech
import org.hablapps.speech._
import org.hablapps.speech.serializer._
import speech.serializer.SerializableComponent

import org.hablapps.dofollow
import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.administration._
import org.hablapps.dofollow.portal.administration.projectModel._
import org.hablapps.dofollow.portal.project._
import org.hablapps.dofollow.portal.project.task._

class CancelTaskTest(System: speech.System with DoFollowSystem with react.Debug) extends FunSpec with ShouldMatchers with BeforeAndAfter {
	describe( "CancelTaskTest"){
		it("Cancel Task") {

		  import System._
		
		  val Output(project1, admin1, task1) = reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    dep2 				<- Initiate2(Department(_departmentName = "Admins", _name = Some("37")), portal1)
		    modelPro1 			<- Initiate2(ProjectModel(_projectName = "ProjectType 1", _name = Some("1")), administration)
		    modelTask1 			<- Initiate2(TaskModel(_taskName = "Task 1", 
					    			_description = "Description",
					    			_dependencies = Set(),
					    			_duration = 3,
					    			_department = "dep1",
					    			_waitingDate = None,
					    			_temporalDependence = None, 
					    			_temporalDuration = None), modelPro1)
		    project1 			<- Initiate2(Project(_persistent = true, _projectAdmin = "12", _projectName = "Project 1"), portal1)
		    operator1 			<- Play2(Operator(_persistent = true, _substatus = Some(Hidden), _name = Some("12")), dep2)
		    task1 				<- Initiate2(Task(_persistent = true, _taskName = "Task 1", _description = "Description",
		    									_duration = 3, _launchType = "A", _deadline = Some(0),
		    									_substatus = Some(Executing), _solution = "Empty"), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    responsible1 		<- Play3(Responsible(_persistent = true), admin1, task1)
		  } yield (project1, admin1, task1))
		
		  val NextState(obtained) = attempt(Say(admin1, project1, CancelTask(_old = Some(task1))))
		
		  reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    dep2 				<- Initiate2(Department(_departmentName = "Admins", _name = Some("37")), portal1)
		    modelPro1 			<- Initiate2(ProjectModel(_projectName = "ProjectType 1", _name = Some("1")), administration)
		    modelTask1 			<- Initiate2(TaskModel(_taskName = "Task 1", 
					    			_description = "Description",
					    			_dependencies = Set(),
					    			_duration = 3,
					    			_department = "dep1",
					    			_waitingDate = None,
					    			_temporalDependence = None, 
					    			_temporalDuration = None), modelPro1)
		    project1 			<- Initiate2(Project(_persistent = true, _projectAdmin = "12", _projectName = "Project 1"), portal1)
		    operator1 			<- Play2(Operator(_persistent = true, _substatus = Some(Hidden), _name = Some("12")), dep2)
		    task1 				<- Initiate2(Task(_persistent = true, _taskName = "Task 1", _description = "Description",
		    									_duration = 3, _launchType = "A", _deadline = Some(0),
		    									_substatus = Some(Executing), _solution = "Empty"), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    responsible1 		<- Play3(Responsible(_persistent = true), admin1, task1)
		    cancel1 			<- Say(admin1, project1, CancelTask(_old = Some(task1)))
		    _ 					<- Done(cancel1, PERFORMED)
		    _ 					<- Let(task1, "substatus", Cancelled, true)
		    _ 					<- LetWholeExtension(task1, "endDate", 0.toLong)
		    _ 					<- Finish(task1)
		    _ 					<- Abandon(responsible1)

		  } yield ())
		
		  obtained should be(getState())
		}

		it("Cancel Task with dependencies") {

		  import System._
		
		  val Output(project1, admin1, task1) = reset(for {
		    		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
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
		    setUp1 				<- Say(admin1, portal1, SetUpProject(__new = Some(Project(_projectName = "Project 1")), 
                  										_numProjectAdmin = "12", _codProject = "1"))
		    _ 					<- Done(setUp1, PERFORMED)
		    project1 			<- Initiate2(Project(_persistent = true, _projectAdmin = "12", _projectName = "Project 1"), portal1)
		    operator1 			<- Play2(Operator(_persistent = true, _substatus = Some(Hidden), _name = Some("12")), dep2)
		    task1 				<- Initiate2(Task(_persistent = true, _name = Some("t1"), _taskName = "Task 1", _description = "Description",
		    									_duration = 3, _launchType = "A", _deadline = Some(0),
		    									_substatus = Some(Executing), _solution = "Empty"), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    task2 				<- Initiate2(Task(_persistent = true, _name = Some("t2"), _taskName = "Task 2", _description = "description 2",
		    									_duration = 3, _substatus = Some(Waiting)), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    _ 					<- Let(task2.context += task1)
		    _ 					<- Let(task1.subinteraction += task2)
		    _ 					<- Let(task2.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task2)		    
		    responsible1 		<- Play3(Responsible(_persistent = true), admin1, task1)
		  } yield (project1, admin1, task1))
		
		  val NextState(obtained) = attempt(Say(admin1, project1, CancelTask(_old = Some(task1))))
		
		  reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
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
		    setUp1 				<- Say(admin1, portal1, SetUpProject(__new = Some(Project(_projectName = "Project 1")), 
                  										_numProjectAdmin = "12", _codProject = "1"))
		    _ 					<- Done(setUp1, PERFORMED)
		    project1 			<- Initiate2(Project(_persistent = true, _projectAdmin = "12", _projectName = "Project 1"), portal1)
		    operator1 			<- Play2(Operator(_persistent = true, _substatus = Some(Hidden), _name = Some("12")), dep2)
		    task1 				<- Initiate2(Task(_persistent = true, _name = Some("t1"), _taskName = "Task 1", _description = "Description",
		    									_duration = 3, _launchType = "A", _deadline = Some(0),
		    									_substatus = Some(Executing), _solution = "Empty"), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    task2 				<- Initiate2(Task(_persistent = true, _name = Some("t2"), _taskName = "Task 2", _description = "description 2",
		    									_duration = 3, _substatus = Some(Waiting)), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    _ 					<- Let(task2.context += task1)
		    _ 					<- Let(task1.subinteraction += task2)
		    _ 					<- Let(task2.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task2)		    
		    responsible1 		<- Play3(Responsible(_persistent = true), admin1, task1)
		    cancel1 			<- Say(admin1, project1, CancelTask(_old = Some(task1)))
		    _ 					<- Done(cancel1, PERFORMED)
		    _ 					<- Let(task1, "substatus", Cancelled, true)
		    _ 					<- Let(task2, "substatus", Executing, true)
			_					<- LetWholeExtension(task2, "startDate" , now)
			_ 					<- Let(task2, "deadline" ,345600000.toLong, true)
			responsible2 		<- Play3(Responsible(_persistent = true), admin1, task2)
		    _ 					<- LetWholeExtension(task1, "endDate", 0.toLong)
		    _ 					<- Finish(task1)
		    _ 					<- Abandon(responsible1)

		  } yield ())
		
		  obtained should be(getState())
		}

		it("Cancel Task of a cancelled task") {

		  import System._
		
		  val Output(project1, admin1, task1) = reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    dep2 				<- Initiate2(Department(_departmentName = "Admins", _name = Some("37")), portal1)
		    modelPro1 			<- Initiate2(ProjectModel(_projectName = "ProjectType 1", _name = Some("1")), administration)
		    modelTask1 			<- Initiate2(TaskModel(_taskName = "Task 1", 
					    			_description = "Description",
					    			_dependencies = Set(),
					    			_duration = 3,
					    			_department = "dep1",
					    			_waitingDate = None,
					    			_temporalDependence = None, 
					    			_temporalDuration = None), modelPro1)
		    project1 			<- Initiate2(Project(_persistent = true, _projectAdmin = "12", _projectName = "Project 1"), portal1)
		    operator1 			<- Play2(Operator(_persistent = true, _substatus = Some(Hidden), _name = Some("12")), dep2)
		    task1 				<- Initiate2(Task(_persistent = true, _taskName = "Task 1", _description = "Description",
		    									_duration = 3, _launchType = "A", _deadline = Some(259200000.toLong),
		    									_substatus = Some(Cancelled), _solution = "Empty"), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    responsible1 		<- Play3(Responsible(_persistent = true), admin1, task1)
		  } yield (project1, admin1, task1))
		
		  val NextState(obtained) = attempt(Say(admin1, project1, CancelTask(_old = Some(task1))))
		
		  reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    dep2 				<- Initiate2(Department(_departmentName = "Admins", _name = Some("37")), portal1)
		    modelPro1 			<- Initiate2(ProjectModel(_projectName = "ProjectType 1", _name = Some("1")), administration)
		    modelTask1 			<- Initiate2(TaskModel(_taskName = "Task 1", 
					    			_description = "Description",
					    			_dependencies = Set(),
					    			_duration = 3,
					    			_department = "dep1",
					    			_waitingDate = None,
					    			_temporalDependence = None, 
					    			_temporalDuration = None), modelPro1)
		    project1 			<- Initiate2(Project(_persistent = true, _projectAdmin = "12", _projectName = "Project 1"), portal1)
		    operator1 			<- Play2(Operator(_persistent = true, _substatus = Some(Hidden), _name = Some("12")), dep2)
		    task1 				<- Initiate2(Task(_persistent = true, _taskName = "Task 1", _description = "Description",
		    									_duration = 3, _launchType = "A", _deadline = Some(259200000.toLong),
		    									_substatus = Some(Cancelled), _solution = "Empty"), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    responsible1 		<- Play3(Responsible(_persistent = true), admin1, task1)
		    cancel1 			<- Say(admin1, project1, CancelTask(_old = Some(task1)))
		    _ 					<- Done(cancel1, PROHIBITED)
		  } yield ())
		
		  obtained should be(getState())
		}

		it("Cancel Task of a finished task") {

		  import System._
		
		  val Output(project1, admin1, task1) = reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    dep2 				<- Initiate2(Department(_departmentName = "Admins", _name = Some("37")), portal1)
		    modelPro1 			<- Initiate2(ProjectModel(_projectName = "ProjectType 1", _name = Some("1")), administration)
		    modelTask1 			<- Initiate2(TaskModel(_taskName = "Task 1", 
					    			_description = "Description",
					    			_dependencies = Set(),
					    			_duration = 3,
					    			_department = "dep1",
					    			_waitingDate = None,
					    			_temporalDependence = None, 
					    			_temporalDuration = None), modelPro1)
		    project1 			<- Initiate2(Project(_persistent = true, _projectAdmin = "12", _projectName = "Project 1"), portal1)
		    operator1 			<- Play2(Operator(_persistent = true, _substatus = Some(Hidden), _name = Some("12")), dep2)
		    task1 				<- Initiate2(Task(_persistent = true, _taskName = "Task 1", _description = "Description",
		    									_duration = 3, _launchType = "A", _deadline = Some(259200000.toLong),
		    									_substatus = Some(Finished), _solution = "Empty"), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    responsible1 		<- Play3(Responsible(_persistent = true), admin1, task1)
		  } yield (project1, admin1, task1))
		
		  val NextState(obtained) = attempt(Say(admin1, project1, CancelTask(_old = Some(task1))))
		
		  reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    dep2 				<- Initiate2(Department(_departmentName = "Admins", _name = Some("37")), portal1)
		    modelPro1 			<- Initiate2(ProjectModel(_projectName = "ProjectType 1", _name = Some("1")), administration)
		    modelTask1 			<- Initiate2(TaskModel(_taskName = "Task 1", 
					    			_description = "Description",
					    			_dependencies = Set(),
					    			_duration = 3,
					    			_department = "dep1",
					    			_waitingDate = None,
					    			_temporalDependence = None, 
					    			_temporalDuration = None), modelPro1)
		    project1 			<- Initiate2(Project(_persistent = true, _projectAdmin = "12", _projectName = "Project 1"), portal1)
		    operator1 			<- Play2(Operator(_persistent = true, _substatus = Some(Hidden), _name = Some("12")), dep2)
		    task1 				<- Initiate2(Task(_persistent = true, _taskName = "Task 1", _description = "Description",
		    									_duration = 3, _launchType = "A", _deadline = Some(259200000.toLong),
		    									_substatus = Some(Finished), _solution = "Empty"), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    responsible1 		<- Play3(Responsible(_persistent = true), admin1, task1)
		    cancel1 			<- Say(admin1, project1, CancelTask(_old = Some(task1)))
		    _ 					<- Done(cancel1, PROHIBITED)
		  } yield ())
		
		  obtained should be(getState())
		}
	}
}