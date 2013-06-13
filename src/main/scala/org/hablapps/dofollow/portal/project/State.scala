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
import org.hablapps.react._
import org.hablapps.speech

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.project._

trait State{ this: speech.Program
	with task.Actions
	with meeting.State
	with task.State
	with administration.State 
	with portal.State =>

	/**		Interaction in order to represent a project. It has tasks and meetings	*/

	trait Project extends Interaction {
		type This = Project
		type Substatus = Nothing
		type ContextCol[x] = Option[x]

		/**		The context is the top interaction Portal	*/

		type Context = Portal
		type SubinteractionCol[x] = Set[x]

		/**		There are two types of subinteractions	
		*
		*	@subinteraction Task 		A project is divided in tasks
		*	@subinteraction Meeting 	There are meetings about a specific project
		*
		*/

		type Subinteraction = Interaction   // Task, Reunión 
		type MemberCol[x] = Traversable[x]
		type Member = Nothing
		type EnvironmentCol[x] = Traversable[x]
		type Environment = Nothing
		type ActionCol[x] = Set[x]

		/**	There are some actions that can be performed in a project
		*
		*	@action 	SetUpTask			Creates a new task
		*	@action 	CloseTask			Finishes a task as a Completed
		*	@action 	CancelTask			Finishes a task as a Cancelled
		*   @action 	SetUpMeeting 		Creates a new meeting
		*
		*/

		type Action = SocialAction

		/** Code of the project admin
		*
		* @kind input
		*
		*/

		val projectAdmin:String

		/** Name of the project
		*
		* @kind input
		*
		*/

		val projectName:String

		/** Progress of this project
		*
		* @kind local
		*
		*/

		val progress:Long

		/** Date when this project was started
		*
		* @kind local
		*
		*/

		val startDate:Long

		/** Date when this project was closed
		*
		* @kind local
		*
		*/

		val endDate:Long

		def portal = context.head
		def tasks = subinteraction.alias[Task]
	}
	
	implicit val Project = builder[Project]

	/** 	Creates a new task 	*/

	trait SetUpTask extends SetUp {
		type This = SetUpTask
		type Substatus = Nothing

		/**		The context is a project	*/

		type Context = Project

		/**		The performer is the Admin	*/

		type Performer = Admin
		type Addressee = Nothing
		type New = Task

		/** Department of this new task
		*
		* @kind input
		*
		*/

		val department:String

		def project = context.head

		/**		The project has to be open	*/

		override def permitted(implicit state: State) = 
			Some(project.status.get == OPEN)
	
	    def NewE = implicitly[Evidence[Task]]
	    def BuilderE: Builder[Task] = Task
	}
	
	implicit val SetUpTask = builder[SetUpTask]

	/**		Finishes a task as a Completed	*/

	trait CloseTask extends SpeechAct {
		type This = CloseTask
		type Substatus = Nothing

		/**		The context is a project	*/

		type Context = Project

		/**		There are two possible performers
		*
		*	@performer Responsible 		Agent in charge of a task
		*	@performer Admin	Admin of the application
		*/

		type Performer = Agent {type Perform >: CloseTask}
		type Addressee = Nothing

		/** Task will be closed
		*
		* @kind local 	
		*
		*/

		val old:Option[$[Task]]

		def task = old.head
		def user = performer.head

		/* 	Changes the substatus to Completed, updates the finish date,
		*   checks new dependencies and closes the task
		*/

		override def purpose(implicit state: State) = {
			Sequence(
				Let(task, "substatus" , Finished, true),
				LetWholeExtension(task, "endDate", now),
				CheckNextDependencies(task),
				Finish(task)
			)
		}

		/**		Only if the performer is the task Responsible or the Admin	*/

		override def empowered(implicit state: State) = user match {
			case x: $[Responsible] if x.isA[Responsible] => x == task.responsible
			case x: $[Admin] if x.isA[Admin] => true
			case _ => false
		}

		/**		The task has to be excuted and it hasn't an empty solution */

		override def permitted(implicit state: State) = 
			Some(task.substatus == Some(Executing) && task.solution != "")
	}
	
	implicit val CloseTask = builder[CloseTask]

	/**		Cancels a task	*/

	trait CancelTask extends SpeechAct {
		type This = CancelTask
		type Substatus = Nothing
		type Context = Project
		type Performer = Admin
		type Addressee = Nothing

		/** Task will be cancelled
		*
		* @kind local 	
		*
		*/

		val old:Option[$[Task]]

		def task = old.head


		/* 	Changes the substatus to Cancelled, updates the finish date,
		*   checks new dependencies and closes the task
		*/

		override def purpose(implicit state: State) = {
			Sequence(
				Let(old.head, "substatus" , Cancelled, true),
				LetWholeExtension(old.head, "endDate", now),
				Let(task, "deadline", now, true),
				CheckNextDependencies(task),
				Finish(task)
			)
		}

		/**		The task has to be excuted or pending to be executed */

		override def permitted(implicit state: State) = 
			Some(task.substatus != Some(Cancelled) && task.substatus != Some(Finished))
			
	}
	
	implicit val CancelTask = builder[CancelTask]	

	/**		Initiates a new meeting	of this project */

	trait SetUpMeeting extends SetUp {
		type This = SetUpMeeting
		type Substatus = Nothing
		type Context = Project
		type Performer = Admin
		type Addressee = Nothing
		type New = Meeting
	
	    def NewE = implicitly[Evidence[Meeting]]
	    def BuilderE: Builder[Meeting] = Meeting
	}
	
	implicit val SetUpMeeting = builder[SetUpMeeting]

}