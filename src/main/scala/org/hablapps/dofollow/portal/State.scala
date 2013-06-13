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
import org.hablapps.react._
import org.hablapps.react.updatable._
import org.hablapps.speech

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.project._

trait State{ this: speech.Program
	with administration.State
	with department.State
	with task.State
	with project.State
	with meeting.State =>

	/**
	*
	*	It's the top interaction. It has projects, administration and areas
	*	
	*/

	trait Portal extends Interaction {
		type This = Portal
		type Substatus = Nothing
		type ContextCol[x] = Traversable[x]
		type Context = Nothing
		type SubinteractionCol[x] = Set[x]

		/** The subinteractions are projects and areas
		*
		*	@subinteraction Department 		Section in order to organize the operators
		*	@subinteraction Project 		Projects in the app.
		*	@subinteraction Administration 	Interaction for managing the application
		*
		*/

		type Subinteraction = Interaction
		type MemberCol[x] = Traversable[x]
		type Member = Nothing
		type EnvironmentCol[x] = Traversable[x]
		type Environment = Nothing
		type ActionCol[x] = Set[x]
		type Action = SocialAction

		/**	There are some actions that can be performed in a portal
		*
		*	@action 	SetUpProject			Creates a new project
		*	@action 	CloseProject			Finishes a project
		*
		*/

		def departments = subinteraction.alias[Department]
		def meetings = subinteraction.alias[Meeting]
		def projects = subinteraction.alias[Project]
		def administration = subinteraction.alias[Administration].head
	}

	implicit val Portal = builder[Portal]

	/**		Create a new project 	*/

	trait SetUpProject extends SetUp {
		type This = SetUpProject
		type Substatus = Nothing

		/**		The context is the top interaction Portal	*/

		type Context = Portal

		/**		The performer is the Admin 	*/

		type Performer = Admin
		type Addressee = Nothing
		type New = Project

		def portal = context.head
		def project = _new.head

		/** Code to identify the project admin
		*
		* @kind input
		*
		*/

		val numProjectAdmin:String

		/** Code to identify the project
		*
		* @kind input
		*
		*/

		val codProject:String

		/*	Returns operator with the adminCode	*/

	    def operator(implicit state: State): Option[Operator] =
	    	portal.departments.flatMap {
	    		case department: $[Department] => department.member
	    	}.filter(_.name.get == numProjectAdmin).toList match {
				case List(operator) => Some(operator)
				case _ => None
			}

		/*	Returns the Admin Department	*/

		def getAdminDepartment(implicit state: State): $[Department] =
			portal.departments.filter(_.name.get == adminDepartment.toString).head

		/**		
		*
		*	If adminCode is a existing adminCode then the user has to stay
		*	in the Admin Department.
		*	If adminCode isn't a existing adminCode then a role of operator in
		*	Admin Department will be created.
		*
		*/

		override def empowered(implicit state: State) = {
			val _operator = operator
			numProjectAdmin != "" &&
			(!_operator.isDefined || _operator.get.context.get == getAdminDepartment && _operator.get.status.get == PLAYING)
	    }

	    def NewE = implicitly[Evidence[Project]]
	    def BuilderE: Builder[Project] = Project
	}

	implicit val SetUpProject = builder[SetUpProject]

	/**		Close a project 	*/

	trait CloseProject extends Close {
		type This = CloseProject
		type Substatus = Nothing

		/**		The context is the top interaction Portal	*/

		type Context = Portal

		/**		The performer is the Admin	*/

		type Performer = Admin
		type Addressee = Nothing
		type Old = Project
	}

	implicit val CloseProject = builder[CloseProject]

}