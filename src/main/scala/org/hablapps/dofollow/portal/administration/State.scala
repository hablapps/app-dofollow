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

package org.hablapps.dofollow.portal.administration

import org.hablapps.updatable._
import org.hablapps.react._
import org.hablapps.speech

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.project._
import org.hablapps.dofollow.portal.administration._

trait State{ this: speech.Program
	with projectModel.State
	with meeting.State
	with department.State 
	with task.State 
	with project.State
	with portal.State => 

	/**
	*
	*	Responsible interaction for managing the application.
	*	In this interaction, there is an only agent called Admin.
	*
	*/

	trait Administration extends Interaction {
		type This = Administration
		type Substatus = Nothing
		type ContextCol[x] = Option[x]

		/**		The context of the Administration is Portal	*/

		type Context = Portal
		type SubinteractionCol[x] = Traversable[x]

		/**		The subinteractions are the models of each project	*/

		type Subinteraction = ProjectModel
		type MemberCol[x] = Option[x]

		/**		The only member in Administration is the Admin	*/

		type Member = Admin
		type EnvironmentCol[x] = Traversable[x]
		type Environment = Nothing
		type ActionCol[x] = Traversable[x]

		/** There are an actions that can be performed in administration
		*	
		* @action ChangeAdmin 	Change the admin profile
		*
		*/ 

		type Action = SocialAction

		def projectModels = subinteraction.alias[ProjectModel]
		def admin = member.alias[Admin].head
	}
	
	implicit val Administration = builder[Administration]

	/**
	*
	* 	Person with the ability to perform any action on the application. 
	*	The administrator has privileges over other agents.
	*
	*/

	trait Admin extends Agent {
		type This = Admin
		type Substatus = Nothing

		/**		The context of the Admin is Administration	*/

		type Context = Administration
		type PlayerCol[x] = Traversable[x]
		type Player = Nothing
		type RoleCol[x] = Set[x]

		/**	Admin can be Responsible, Moderator, Transmitter and Receiver	
		*
		* @role Responsible 	Agent in charge of a task
		* @role Moderator		Admin of a Meeting
		* @role Issuer			Agent sends a message to another agent
		* @role Receiver		Agent receives a message to another agent
		* 
		*/

		type Role = Agent
		type PerformCol[x] = Set[x]

		/**	Admin can perform all the actions	
		*
		* @perform SetUpProject 			Create a new project
		* @perform CloseProject				Finish a project
		* @perform SetUpTask				Create a new task
		* @perform CloseTask				Finish a task like closed
		* @perform CancelTask				Finish a task like canceled
		* @perform SetUpMeeting				Create a new meeting
		* @perform ScheduleTask				Change date of a task.	
		* @perform InitiateTask				Initiate a task manually
		* @perform Comment					Create a new comment
		* @perform AssignResponsible	 	Create a new responsible for a task
		* @perform CreateAnnex 				Create a new resource Annex
		* @perform DestroyAnnex				Delete a resource Annex
		* @perform JoinResponsibleAdmin 	Create a new role of Responsible
		* @perform FireResponsible 			Delete a role of Responsible
		* @perform SetUpDiscussion 			Create a new interaction Messaging
		* @perform Reply 					Create a new comment in the discussion		
		* @perform ChangeAdmin 				Change the admin profile	
		*/

		type Perform = SocialAction  

		/** Admin's email
		*
		* @kind input
		*
		*/

		val email:String

		/** Admin's name
		*
		* @kind input
		*
		*/

		val forename:String

		/** Admin's surname
		*
		* @kind input
		*
		*/

		val surname:String

		def fullName = forename + "" + surname

		def moderators = role.alias[Moderator]
	}
	
	implicit val Admin = builder[Admin]

	/**		Change the admin profile	*/

	trait ChangeAdmin extends SpeechAct {
		type This = ChangeAdmin
		type Substatus = Nothing

		/**		The context is the administration	*/

		type Context = Administration

		/**		The performer is the admin	*/

		type Performer = Admin
		type Addressee = Nothing

		/** New forename
		*
		* @kind input
		*
		*/

		val forename: String

		/** New surname
		*
		* @kind input
		*
		*/

		val surname: String
		
		/** New email
		*
		* @kind input
		*
		*/

		val email: String
		
		/** New name
		*
		* @kind input
		*
		*/

		val numAdmin: String

		def admin = performer.head
		def administration = context.head

		/* Change Admin updates email, forename, surname and code */

		override def purpose(implicit state: State) = 
			Sequence(
				LetWholeExtension(admin, "email", email),
				LetWholeExtension(admin, "forename", forename),
				LetWholeExtension(admin, "surname", surname),
				Let(admin, "name", numAdmin, true)
			)
	}
	
	implicit val ChangeAdmin = builder[ChangeAdmin]
	
	
}