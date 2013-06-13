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

package org.hablapps.dofollow

import org.hablapps.updatable._
import org.hablapps.react
import org.hablapps.react._
import org.hablapps.serializer.{serializeMe}
import org.hablapps.speech
import org.hablapps.speech._
import org.hablapps.speech.serializer._

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.department._
import org.hablapps.dofollow.portal.project._
import org.hablapps.dofollow.portal.administration._
import org.hablapps.dofollow.portal.administration.projectModel._
import org.hablapps.dofollow.portal.project.task._

import scala.util.parsing.json._

/*  System & Program  */

  trait DoFollowSystem extends speech.PlainSystem 
    with portal.State
    with administration.State
    with projectModel.State
    with taskModel.State
    with department.State
    with project.State
    with task.State
    with meeting.State
    with discussion.State
    with portal.Rules
    with department.Rules
    with project.Rules
    with meeting.Rules
    with task.Rules
    with discussion.Rules
    with portal.Actions
    with task.Actions
    with discussion.Actions{
      
      println("Welcome to Do & Follow")
  }

  object DoFollowProgram extends DoFollowSystem

  trait DoFollowConsoleSystem extends DoFollowSystem with speech.web.PlainSystem{
    serializeMe[this.type]
  }

