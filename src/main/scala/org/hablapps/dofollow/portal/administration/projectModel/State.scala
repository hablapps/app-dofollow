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

package org.hablapps.dofollow.portal.administration.projectModel

import org.hablapps.updatable._
import org.hablapps.react._
import org.hablapps.speech

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._

trait State{ this: speech.Program
	with administration.State 
	with taskModel.State => 

	/**
	*
	*	A kind of project.
	*	It has all the information of this kind of project.
	*
	*/

	trait ProjectModel extends Interaction {
		type This = ProjectModel
		type Substatus = Nothing
		type ContextCol[x] = Option[x]

		/**		The context is the Administration	*/

		type Context = Administration
		type SubinteractionCol[x] = Set[x]

		/**		The subinteractions are the task of this kind of project	*/

		type Subinteraction = TaskModel
		type MemberCol[x] = Traversable[x]
		type Member = Nothing
		type EnvironmentCol[x] = Traversable[x]
		type Environment = Nothing
		type ActionCol[x] = Traversable[x]
		type Action = Nothing

		/** Project's name
		*
		* @kind input
		*
		*/

		val projectName:String

		def tasks = subinteraction.alias[TaskModel]
	}
	
	implicit val ProjectModel = builder[ProjectModel]


}	