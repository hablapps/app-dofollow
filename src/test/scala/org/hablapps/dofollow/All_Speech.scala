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

import org.scalatest.BeforeAndAfter
import org.scalatest.Suite
import org.scalatest.Suites
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import scala.collection.immutable.HashSet
import scala.collection.immutable.HashMap

import org.hablapps.dofollow
import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.administration._
import org.hablapps.dofollow.portal.administration.projectModel._
import org.hablapps.dofollow.portal.project._
import org.hablapps.dofollow.portal.project.task._

import org.hablapps.dofollow.test._
import org.hablapps.dofollow.test.portal._
import org.hablapps.dofollow.test.portal.department._
import org.hablapps.dofollow.test.portal.project._
import org.hablapps.dofollow.test.portal.project.meeting._
import org.hablapps.dofollow.test.portal.project.task._

class All_Speech extends Suites(
  new InitiatePortal(DoFollowProgram),
  new SetUpProjectTest(DoFollowProgram),
  new CloseProjectTest(DoFollowProgram),
  new ScheduleTaskTest(DoFollowProgram),
  new SetUpTaskTest(DoFollowProgram),
  new CloseTaskTest(DoFollowProgram),
  new CancelTaskTest(DoFollowProgram),
  new SetUpMeetingTest(DoFollowProgram),
  new AssignInviteeTest(DoFollowProgram),
  new LeaveInviteeTest(DoFollowProgram),
  new FireInviteeTest(DoFollowProgram),
  new JoinAttendeeTest(DoFollowProgram),
  new LeaveAttendeeTest(DoFollowProgram),
  new FireAttendeeTest(DoFollowProgram),
  new CreateMinutesTest(DoFollowProgram),
  new Play2OperatorTest(DoFollowProgram),
  new ActiveOperatorTest(DoFollowProgram))

class DepartmentsTest extends Suites(
  new Play2OperatorTest(DoFollowProgram),
  new ActiveOperatorTest(DoFollowProgram))

class ProjectsTest extends Suites(
  new SetUpProjectTest(DoFollowProgram),
  new CloseProjectTest(DoFollowProgram))

class MeetingsTest extends Suites(
  new SetUpMeetingTest(DoFollowProgram),
  new AssignInviteeTest(DoFollowProgram),
  new LeaveInviteeTest(DoFollowProgram),
  new FireInviteeTest(DoFollowProgram),
  new JoinAttendeeTest(DoFollowProgram),
  new LeaveAttendeeTest(DoFollowProgram),
  new FireAttendeeTest(DoFollowProgram),
  new CreateMinutesTest(DoFollowProgram))

class TasksTest extends Suites(
  new ScheduleTaskTest(DoFollowProgram),
  new SetUpTaskTest(DoFollowProgram),
  new CloseTaskTest(DoFollowProgram),
  new CancelTaskTest(DoFollowProgram))