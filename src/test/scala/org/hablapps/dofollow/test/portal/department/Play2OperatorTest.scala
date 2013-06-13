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

package org.hablapps.dofollow.test.portal.department

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

class Play2OperatorTest(System: speech.System with DoFollowSystem with react.Debug) extends FunSpec with ShouldMatchers with BeforeAndAfter {
	describe( "Play2OperatorTest"){
		it("Play 2 of an Operator") {

		  import System._

		  // turn_on_log = true
		  // show_causes = true
		  // show_empty_reactions = true
		
		  val Output(dep1) = reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    operator1 			<- Play2(Operator(_name = Some("1")), dep1)
		  } yield (dep1))
		
		  val NextState(obtained) = attempt(Play2(Operator(_name = Some("2")), dep1))
		
		  reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    operator1 			<- Play2(Operator(_name = Some("1")), dep1)
		    operator2 			<- Play2(Operator(_name = Some("2")), dep1)
		    _ 					<- LetWholeExtension(operator2, "persistent", true)
		  } yield ())
		
		  obtained should be(getState())
		}

		it("Play 2 FAIL (same cod)") {

		  import System._

		  // turn_on_log = true
		  // show_causes = true
		  // show_empty_reactions = true
		
		  val Output(dep1) = reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    operator1 			<- Play2(Operator(_name = Some("1")), dep1)
		  } yield (dep1))
		
		  attempt(Play2(Operator(_name = Some("1")), dep1)) should be(None)

		}

		it("Play 2 FAIL (with the Admin code)") {

		  import System._

		  // turn_on_log = true
		  // show_causes = true
		  // show_empty_reactions = true
		
		  val Output(dep1) = reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    operator1 			<- Play2(Operator(_name = Some("1")), dep1)
		  } yield (dep1))
		
		  attempt(Play2(Operator(_name = Some("0")), dep1)) should be(None)

		}

		it("Play 2 FAIL (with an empty code)") {

		  import System._

		  // turn_on_log = true
		  // show_causes = true
		  // show_empty_reactions = true
		
		  val Output(dep1) = reset(for {
		    portal1 			<- Initiate(Portal())
		    administration 		<- Initiate2(Administration(), portal1)
		    admin1 				<- Play2(Admin(_forename = "forename", _surname = "SurnameAdmin", _name = Some("0")), administration)
		    dep1 				<- Initiate2(Department(_departmentName = "Department 1", _name = Some("dep1")), portal1)
		    operator1 			<- Play2(Operator(_name = Some("1")), dep1)
		  } yield (dep1))
		
		  attempt(Play2(Operator(_name = Some("")), dep1)) should be(None)

		}
	}
}