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

package org.hablapps.dofollow.portal.project.task.discussion

import org.hablapps.updatable._
import org.hablapps.react._
import org.hablapps.speech

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.project._

trait State{ this: speech.Program
	with task.State =>

	/**		Interaction with the comments about a task	*/

	trait Discussion extends Interaction {
		type This = Discussion
		type Substatus = ZombieTaskState
		type ContextCol[x] = Set[x]

		/**		The context is a task	*/

		type Context = Task
		type SubinteractionCol[x] = Traversable[x]
		type Subinteraction = Nothing
		type MemberCol[x] = Set[x]

		/**		There are two types of members
		*
		*		@member 	Issuer 	 	First talker
		*		@member 	Receiver 	Second talker
		*
		*/

		type Member = Agent {type Context >: Discussion}
		type EnvironmentCol[x] = Traversable[x]
		type Environment = Nothing
		type ActionCol[x] = Set[x]

		/** 
		*
		* 	@action Reply 	Replies a comment of the other agent
		*
		*/

		type Action = SocialAction

		/** Date of the discussion
		*
		* @kind local
		*
		*/

		val date:Long

		/** First message
		*
		* @kind input
		*
		*/

		val message:String

		def task = context.head
		def issuer = member.alias[Issuer].head
	}
	
	implicit val Discussion = builder[Discussion]
	
	/**		The first talker in the discussion	*/

	trait Issuer extends Agent {
		type This = Issuer
		type Substatus = Nothing

		/**		The context is the discussion	*/

		type Context = Discussion
		type PlayerCol[x] = Option[x]

		/**		The player can be anyone	*/		

		type Player = Agent {type Role >: Issuer}
		type RoleCol[x] = Traversable[x]
		type Role = Nothing
		type PerformCol[x] = Set[x]

		/** 
		*
		* 	@perform Reply 	Reply a comment of the other agent
		*
		*/

		type Perform = Reply

		def discussion = context.head
	}
	
	implicit val Issuer = builder[Issuer]		

	/**		The receiver of the first message	*/

	trait Receiver extends Agent {
		type This = Receiver
		type Substatus = Nothing

		/**		The context is the discussion	*/

		type Context = Discussion
		type PlayerCol[x] = Option[x]

		/**		The player can be anyone	*/			

		type Player = Agent {type Role >: Receiver}
		type RoleCol[x] = Traversable[x]
		type Role = Nothing
		type PerformCol[x] = Set[x]

		/** 
		*
		* 	@perform Reply 	Reply a comment of the other agent
		*
		*/

		type Perform = Reply

		def discussion = context.head
	}
	
	implicit val Receiver = builder[Receiver]

	/**		Replies a message	*/
	
	trait Reply extends SpeechAct {
		type This = Reply
		type Substatus = Nothing

		/**		The context is the discussion	*/

		type Context = Discussion 

		/**		The performer can be anyone	*/	

		type Performer = Agent {type Perform >: Reply}
		type Addressee = Nothing	

		def talker = performer.head

		/** Date of the message
		*
		* @kind local
		*
		*/

		val date:Long

		/**		The task of the discussion has to be executed	*/

		override def permitted(implicit state: State) = Some(context.head.context.head.substatus.get == Executing)
	}

	implicit val Reply = builder[Reply]
	
}