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

package org.hablapps.dofollow.test.portal.project.meeting

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

class CreateMinutesTest(System: speech.System with DoFollowSystem with react.Debug) extends FunSpec with ShouldMatchers with BeforeAndAfter {
	describe( "CreateMinutesTest"){
		it("Create Minutes") {

		  import System._
		
		  val Output(meeting1, moderator1) = reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    project1 			<- Initiate2(Project(_persistent = true, _projectAdmin = "12", _projectName = "Project 1"), portal1)
		    task1 				<- Initiate2(Task(_persistent = true, _taskName = "Task 1", _description = "Description",
		    									_duration = 3, _launchType = "A", _deadline = Some(259200000.toLong),
		    									_substatus = Some(Executing), _startDate = 0), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    responsible1 		<- Play3(Responsible(_persistent = true), admin1, task1)
		    meeting1 			<- Initiate2(Meeting(_persistent = true, 
		  										_date = 123,
		    									_room = "3",
		    									_reason = "Reason1",
		    									_substatus = Some(Scheduled)), project1)
		    moderator1 			<- Play3(Moderator(_persistent = true), admin1, meeting1)
		  } yield (meeting1, moderator1))
		
		  val NextState(obtained) = attempt(Say(moderator1, meeting1, CreateMinutes(__new = Some(Minutes()))))
		
		  reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    project1 			<- Initiate2(Project(_persistent = true, _projectAdmin = "12", _projectName = "Project 1"), portal1)
		    task1 				<- Initiate2(Task(_persistent = true, _taskName = "Task 1", _description = "Description",
		    									_duration = 3, _launchType = "A", _deadline = Some(259200000.toLong),
		    									_substatus = Some(Executing), _startDate = 0), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    responsible1 		<- Play3(Responsible(_persistent = true), admin1, task1)
		    meeting1 			<- Initiate2(Meeting(_persistent = true, 
		  										_date = 123,
		    									_room = "3",
		    									_reason = "Reason1",
		    									_substatus = Some(Scheduled)), project1)
		    moderator1 			<- Play3(Moderator(_persistent = true), admin1, meeting1)
		    create1 			<- Say(moderator1, meeting1, CreateMinutes(__new = Some(Minutes())))
		    _ 					<- Done(create1, PERFORMED)
		    minutes1 			<- NewR(Minutes(), meeting1)
		    _ 					<- Let(minutes1, "creator", moderator1, true)
		    _ 					<- LetWholeExtension(minutes1, "uploadDate", 0.toLong)
		    _ 					<- LetWholeExtension(minutes1, "persistent", true)
		  } yield ())
		
		  obtained should be(getState())
		}

		it("Create Minutes in a completed meeting") {

		  import System._
		
		  val Output(meeting1, moderator1) = reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    project1 			<- Initiate2(Project(_persistent = true, _projectAdmin = "12", _projectName = "Project 1"), portal1)
		    task1 				<- Initiate2(Task(_persistent = true, _taskName = "Task 1", _description = "Description",
		    									_duration = 3, _launchType = "A", _deadline = Some(259200000.toLong),
		    									_substatus = Some(Executing), _startDate = 0), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    responsible1 		<- Play3(Responsible(_persistent = true), admin1, task1)
		    meeting1 			<- Initiate2(Meeting(_persistent = true, 
		  										_date = 123,
		    									_room = "3",
		    									_reason = "Reason1",
		    									_substatus = Some(Completed)), project1)
		    moderator1 			<- Play3(Moderator(_persistent = true), admin1, meeting1)
		  } yield (meeting1, moderator1))
		
		  val NextState(obtained) = attempt(Say(moderator1, meeting1, CreateMinutes(__new = Some(Minutes()))))
		
		  reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    project1 			<- Initiate2(Project(_persistent = true, _projectAdmin = "12", _projectName = "Project 1"), portal1)
		    task1 				<- Initiate2(Task(_persistent = true, _taskName = "Task 1", _description = "Description",
		    									_duration = 3, _launchType = "A", _deadline = Some(259200000.toLong),
		    									_substatus = Some(Executing), _startDate = 0), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    responsible1 		<- Play3(Responsible(_persistent = true), admin1, task1)
		    meeting1 			<- Initiate2(Meeting(_persistent = true, 
		  										_date = 123,
		    									_room = "3",
		    									_reason = "Reason1",
		    									_substatus = Some(Completed)), project1)
		    moderator1 			<- Play3(Moderator(_persistent = true), admin1, meeting1)
		    create1 			<- Say(moderator1, meeting1, CreateMinutes(__new = Some(Minutes())))
		    _ 					<- Done(create1, PROHIBITED)
		  } yield ())
		
		  obtained should be(getState())
		}

		it("Create Minutes in a Undone meeting") {

		  import System._
		
		  val Output(meeting1, moderator1) = reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    project1 			<- Initiate2(Project(_persistent = true, _projectAdmin = "12", _projectName = "Project 1"), portal1)
		    task1 				<- Initiate2(Task(_persistent = true, _taskName = "Task 1", _description = "Description",
		    									_duration = 3, _launchType = "A", _deadline = Some(259200000.toLong),
		    									_substatus = Some(Executing), _startDate = 0), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    responsible1 		<- Play3(Responsible(_persistent = true), admin1, task1)
		    meeting1 			<- Initiate2(Meeting(_persistent = true, 
		  										_date = 123,
		    									_room = "3",
		    									_reason = "Reason1",
		    									_substatus = Some(Undone)), project1)
		    moderator1 			<- Play3(Moderator(_persistent = true), admin1, meeting1)
		  } yield (meeting1, moderator1))
		
		  val NextState(obtained) = attempt(Say(moderator1, meeting1, CreateMinutes(__new = Some(Minutes()))))
		
		  reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    project1 			<- Initiate2(Project(_persistent = true, _projectAdmin = "12", _projectName = "Project 1"), portal1)
		    task1 				<- Initiate2(Task(_persistent = true, _taskName = "Task 1", _description = "Description",
		    									_duration = 3, _launchType = "A", _deadline = Some(259200000.toLong),
		    									_substatus = Some(Executing), _startDate = 0), project1)
		    _ 					<- Let(task1.context += dep1)
		    _ 					<- Let(dep1.subinteraction += task1)
		    responsible1 		<- Play3(Responsible(_persistent = true), admin1, task1)
		    meeting1 			<- Initiate2(Meeting(_persistent = true, 
		  										_date = 123,
		    									_room = "3",
		    									_reason = "Reason1",
		    									_substatus = Some(Undone)), project1)
		    moderator1 			<- Play3(Moderator(_persistent = true), admin1, meeting1)
		    create1 			<- Say(moderator1, meeting1, CreateMinutes(__new = Some(Minutes())))
		    _ 					<- Done(create1, PROHIBITED)
		  } yield ())
		
		  obtained should be(getState())
		}

	}
}