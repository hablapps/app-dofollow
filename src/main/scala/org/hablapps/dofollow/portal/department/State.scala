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

package org.hablapps.dofollow.portal.department

import org.hablapps.updatable._
import org.hablapps.react._
import org.hablapps.speech

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal.project._


trait State{ this: speech.Program
	with meeting.State
	with portal.State
	with task.State =>

	/**		Interaction in order to organize operators in different departments	*/

	trait Department extends Interaction {
		type This = Department
		type Substatus = Nothing
		type ContextCol[x] = Option[x]

		/** 	The context is the top interaction Portal	*/

		type Context = Portal
		type SubinteractionCol[x] = Set[x]

		/**		The subinteractios are the task of this department	*/

		type Subinteraction = Task
		type MemberCol[x] = Set[x]

		/**		The members are Operators	*/

		type Member = Operator
		type EnvironmentCol[x] = Traversable[x]
		type Environment = Nothing
		type ActionCol[x] = Traversable[x]
		type Action = SocialAction

		/** Name of the department
		*
		* @kind input
		*
		*/

		val departmentName:String

		def portal = context.head
		def operators = member
	}
	
	implicit val Department = builder[Department]

	/**		Basic agent that can be Responsible of a task	*/
		
	trait Operator extends Agent {
		type This = Operator
		type Substatus = OperatorState

		/**		The context is a department	*/

		type Context = Department
		type PlayerCol[x] = Traversable[x]
		type Player = Nothing
		type RoleCol[x] = Set[x]

		/**	The role of Operator area: Responsible, Attendee and Invitee	
		*
		*	@role Responsible	Agent in charge of a task
		*	@role Attendee		Agent attending a meeting
		*	@role Invitee	 	Agent invited to a meeting
		* 	@role Issuer		Agent sends a message to another agent
		* 	@role Receiver		Agent receives a message to another agent
		*
		*/

		type Role = Agent
		type PerformCol[x] = Set[x]

		/**
		*
		*	@perform 	JoinResponsible			Creates a new role of Responsible in a task
		*	@perform 	JoinAttendee			Creates a new role of Attendee in a meeting
		*	@perform 	Comment					Says a comment in a task
		*	@perform 	SetUpDiscussion			Initiates a discussion
		*	@perform 	Reply					Replies a comment in a discussíon
		*
		*/

		type Perform = SocialAction 

		/** Email of an operator
		*
		* @kind input
		*
		*/

		val email:String

		/** Name of an operator
		*
		* @kind input
		*
		*/

		val forename:String

		/** Surname of an operator
		*
		* @kind input
		*
		*/

		val surname:String

		def department = context.head
		def invitees = role.alias[Invitee]

		def fullName = forename + "" + surname
	}
	
	implicit val Operator = builder[Operator]

	/*	An operator will be Hidden or Visible	*/

	trait OperatorState extends EntityStatus
	case object Enable extends OperatorState
	case object Hidden extends OperatorState	

}
