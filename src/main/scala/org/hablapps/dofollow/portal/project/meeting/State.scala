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

package org.hablapps.dofollow.portal.project.meeting

import org.hablapps.updatable._
import org.hablapps.react._
import org.hablapps.speech

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.project._

trait State{ this: speech.Program
	with task.State
	with project.State
	with department.State
	with portal.State 
	with administration.State =>

	/**		Interaction in order to represent a meeting	*/

	trait Meeting extends Interaction {
		type This = Meeting
		type Substatus = MeetingState
		type ContextCol[x] = Set[x]

		/**		The context is a project	*/

		type Context = Project
		type SubinteractionCol[x] = Traversable[x]
		type Subinteraction = Nothing
		type MemberCol[x] = Set[x]

		/**		There are two types of members	
		*
		*		@member Attendee 	<Agent attending a meeting>
		*		@member Invitee 	<Invited agent in a meeting>
		*		@member Moderator 	<Admin of the meeting>
		*
		*/

		type Member = Agent
		type EnvironmentCol[x] = Set[x]

		/**		The environment is a minutes	*/

		type Environment = Minutes
		type ActionCol[x] = Set[x]

		/**		There are many actions
		*
		*		@action 	LeaveAttendee 				Abandons the rol of attendee
		*		@action 	LeaveInvitee				Abandons the rol of invitee
		*		@action 	CreateMinutes				Creates a new minutes
		*		@action 	DestroyMinutes				Destroys a minutes
		*		@action 	CloseMeeting				Closes a meeting
		*		@action 	CancelMeeting				Cancels a meeting
		*		@action 	ChangeDateMeeting			Creates a new planning
		*		@action 	FireInvitee					Abandons the rol of attendee
		*		@action 	FireAttendee				Abandons the rol of invitee
		*		@action 	JoinAttendee				Plays the rol of attendee
		* 		@action 	AssignInvitee				Plays the rol of invitee
		*
		*/

		type Action = SocialAction

		/** Meeting date
		*
		* @kind local
		*
		*/

		val date:Long

		/** Meeting room
		*
		* @kind local
		*
		*/

		val room:String

		/** Reason for a meeting
		*
		* @kind local
		*
		*/

		val reason:String

		def minutes = environment.alias[Minutes]
		def invitees = member.alias[Invitee]
		def attendees = member.alias[Attendee]
		def moderator = member.alias[Moderator].head

		def project = context.head
	}
	
	implicit val Meeting = builder[Meeting]

	trait MeetingState extends EntityStatus
	case object Scheduled extends MeetingState
	case object Rescheduled extends MeetingState
	case object Undone extends MeetingState 
	case object Completed extends MeetingState

	trait ZombieMeetingState extends EntityStatus
	case object ZombieMeeting extends ZombieMeetingState

	/** 	@declarer When the meeting isn't closed	*/

	declarer[Moderator].of[Meeting](Meeting._date)
	    .permitted {
	      case (mod, reu, newValorAtributo) => implicit state => 
      		Some(reu.substatus.get != Undone || reu.substatus.get != Completed)
	    }

    /** 	@declarer When the meeting isn't closed	*/

	declarer[Moderator].of[Meeting](Meeting._room)
	    .permitted {
	      case (mod, reu, newValorAtributo) => implicit state => 
	      	Some(reu.substatus.get != Undone || reu.substatus.get != Completed)
	    }

    /** 	@declarer When the meeting isn't closed	*/

	declarer[Moderator].of[Meeting](Meeting._reason)
	    .permitted {
	      case (mod, reu, newValorAtributo) => implicit state => 
	    	Some(reu.substatus.get != Undone || reu.substatus.get != Completed) 
	    }

    /** 	@declarer When the meeting isn't closed	*/

	declarer[Moderator].of[Meeting](Meeting._substatus)
	    .permitted {
	      case (mod, reu, newValorAtributo) => implicit state => 
	      	Some(reu.substatus.get == Scheduled || reu.substatus.get == Rescheduled)      
	    }

    /**		Admin of the meeting	*/

	trait Moderator extends Agent {
		type This = Moderator
		type Substatus = ZombieMeetingState

		/**		The context is a meeting	*/

		type Context = Meeting
		type PlayerCol[x] = Option[x]

		/**		The player is the admin of the application	*/

		type Player = Admin
		type RoleCol[x] = Traversable[x]
		type Role = Nothing
		type PerformCol[x] = Set[x]

		/**
		*
		*	@perform 	AssignInvitee				Plays the rol of invitee
		*	@perform 	FireInvitee				Abandons the rol of invitee
		*	@perform 	FireAttendee				Abandons the rol of attendee
		*	@perform 	CreateMinutes				Creates a new minutes
		*	@perform 	DestroyMinutes				Destroys a minutes		
		*	@perform 	CloseMeeting		Closes a meeting
		*	@perform 	CancelMeeting		Cancels a meeting		
		*
		*/


		type Perform = SocialAction 
	}
	
	implicit val Moderator = builder[Moderator]

	trait Attendee extends Agent {
		type This = Attendee
		type Substatus = ZombieMeetingState

		/**		The context is a meeting	*/

		type Context = Meeting
		type PlayerCol[x] = Option[x]

		/**		The player is an opèrator	*/

		type Player = Operator
		type RoleCol[x] = Traversable[x]
		type Role = Nothing
		type PerformCol[x] = Set[x]

		/** An attendee can perform many social actions
		*
		*	@perform 	LeaveAttendee 	Abandons the rol of attendee
		*
		*/

		type Perform = SocialAction

		def meeting = context.head
		def operator = player.head
	}
	
	implicit val Attendee = builder[Attendee]

	trait Invitee extends Agent {
		type This = Invitee
		type Substatus = ZombieMeetingState

		/**		The context is a meeting	*/

		type Context = Meeting
		type PlayerCol[x] = Option[x]

		/**		The player is an opèrator	*/

		type Player = Operator
		type RoleCol[x] = Option[x]
		type Role = Nothing
		type PerformCol[x] = Set[x]

		/** An invitee can perform many social actions
		*
		*	@perform 	LeaveInvitee 				Abandons the rol of invitee
		*	@perform 	ChangeDateMeeting			Creates a new planning
		*
		*/

		type Perform = SocialAction

		def meeting = context.head
		def operator = player.head
	}
	
	implicit val Invitee = builder[Invitee]


	/**		A minutes of a meeting	*/

	trait Minutes extends Resource {
		type This = Minutes
		type Substatus = ZombieMeetingState

		/**		The context is a meeting	*/

		type Context = Meeting
		type CreatorCol[x] = Option[x]

		/**		The creator is the moderator	*/

		type Creator = Moderator
		type OwnerCol[x] = Option[x]

		/**		The owner is the moderator	*/

		type Owner = Moderator

		/** File name
		*
		* @kind input
		*
		*/

		val fileName:String

		/** Upload date
		*
		* @kind local
		*
		*/

		val uploadDate:Long
	}
	
	implicit val Minutes = builder[Minutes]

	/**		Plays the rol of invitee	*/

	trait AssignInvitee extends Assign {
		type This = AssignInvitee
		type Substatus = Nothing

		/**		The context is a meeting	*/

		type Context = Meeting

		/**		The performer is a moderator	*/

		type Performer = Moderator
		type Addressee = Nothing
		type New = Invitee

		/**		The player is an operator	*/

		type Player = Operator              
	
		def EvidenceNew = implicitly[Evidence[New]]
		def BuilderNew: Builder[New] = implicitly[Builder[New]]
		def EvidencePlayer: Evidence[Operator] = implicitly[Evidence[Operator]]
		def BuilderPlayer: Builder[Operator] = implicitly[Builder[Operator]]
	}
	implicit val AssignInvitee = builder[AssignInvitee]

	/**		Abandons the rol of invitee	*/

	trait LeaveInvitee extends Leave {
		type This = LeaveInvitee
		type Substatus = Nothing

		/**		The context is a meeting	*/

		type Context = Meeting

		/**		The performer is the invitee	*/

		type Performer = Invitee  
		type Addressee = Nothing  
	}
	
	implicit val LeaveInvitee = builder[LeaveInvitee]

	/**		Abandons the rol of attendee	*/

	trait FireInvitee extends Fire {
		type This = FireInvitee
		type Substatus = Nothing

		/**		The context is a meeting	*/

		type Context = Meeting

		/** The performer is the moderator 	*/

		type Performer = Moderator
		type Addressee = Nothing
		type Role = Invitee
	}
	
	implicit val FireInvitee = builder[FireInvitee]

	/**		Plays the rol of attendee		*/

	trait JoinAttendee extends Join {
		type This = JoinAttendee
		type Substatus = Nothing

		/**		The context is a meeting	*/

		type Context = Meeting

		/**		The performer is an operator	*/

		type Performer = Operator
		type Addressee = Nothing
		type New = Attendee

		def meeting = context.head
		def operator = performer.head

		/**		Only if the operator has a role of invitee in the meeting	*/

		override def empowered(implicit state: State):Boolean = 
			meeting.invitees.exists(_.player.head == operator)

		/**		Only if the meeting isn't closed	*/	

		override def permitted(implicit state: State) =
			Some(meeting.substatus == Some(Scheduled) || meeting.substatus == Some(Rescheduled))

		def NewE = implicitly[Evidence[New]]
    	def BuilderE: Builder[New] = Attendee
	}

	implicit val JoinAttendee = builder[JoinAttendee]

	/**		Abandons the rol of attendee	*/

	trait LeaveAttendee extends Leave {
		type This = LeaveAttendee
		type Substatus = Nothing

		/**		The context is a meeting	*/

		type Context = Meeting

		/**		The performer is the attendee	*/

		type Performer = Attendee
		type Addressee = Nothing
	}
	
	implicit val LeaveAttendee = builder[LeaveAttendee]
	
	/**		Abandons the rol of invitee	*/

	trait FireAttendee extends Fire {
		type This = FireAttendee
		type Substatus = Nothing

		/**		The context is a meeting	*/

		type Context = Meeting

		/** The performer is the moderator 	*/

		type Performer = Moderator
		type Addressee = Nothing
		type Role = Attendee
	}
	
	implicit val FireAttendee = builder[FireAttendee]

	/** Creates a new minutes 	*/

	trait CreateMinutes extends Create {
		type This = CreateMinutes
		type Substatus = Nothing

		/**		The context is a meeting	*/

		type Context = Meeting

		/** The performer is the moderator 	*/

		type Performer = Moderator
		type Addressee = Nothing
		type New = Minutes

		def meeting = context.head
		def moderator = performer.head
	
		def EvidenceNew = implicitly[Evidence[New]]
	    def BuilderNew = implicitly[Builder[New]]

	    /**		Only if the meeting isn't closed	*/

		override def permitted(implicit state: State) =
			Some(meeting.substatus.head != Completed && meeting.substatus.head != Undone)
	}
	
	implicit val CreateMinutes = builder[CreateMinutes]

	/**		Destroys a minutes	*/

	trait DestroyMinutes extends Destroy {
		type This = DestroyMinutes
		type Substatus = Nothing

		/**		The context is a meeting	*/

		type Context = Meeting

		/**		The performer is the moderator	*/

		type Performer = Moderator
		type Addressee = Nothing
		type Resource = Minutes

		def meeting = context.head

		/**		Only if the meeting isn't closed	*/

		override def permitted(implicit state: State) =
			Some(meeting.substatus.head != Completed && meeting.substatus.head != Undone)
	}
	
	implicit val DestroyMinutes = builder[DestroyMinutes]

	/**		Creates a new planning	*/

	trait ChangeDateMeeting extends SpeechAct {
		type This = ChangeDateMeeting
		type Substatus = Nothing

		/**		The context is a meeting	*/

		type Context = Meeting

		/**		The performer is an invitee	*/

		type Performer = Invitee 
		type Addressee = Nothing 

		def meeting = context.head
		def invitee = performer.head

		/** New meeting date
		*
		* @kind local
		*
		*/

		val newDate:Long

		/**		Only if the invitee is a member of the meeting */

		override def empowered(implicit state: State):Boolean = 
			invitee match {
				case x: $[Invitee] if x.isA[Invitee] => x.context.head == meeting
				case _ => false
			}

		/**		Only if the meeting isn't closed	*/

		override def permitted(implicit state: State) =
			Some(meeting.substatus.head == Scheduled || meeting.substatus.head == Rescheduled)
	}

	implicit val ChangeDateMeeting = builder[ChangeDateMeeting]

	/**		Closes a meeting	*/

    trait CloseMeeting extends SpeechAct {
        type This = CloseMeeting
        type Substatus = Nothing

        /**		The context is a meeting	*/

        type Context = Project

        /**		The performer is the moderator	*/

        type Performer = Moderator
        type Addressee = Nothing

        /**		The meeting that will be closed	*/

        val old : Option[$[Meeting]]

        def meeting = old.get

        override def purpose(implicit state: State) = {
                Sequence(
                        Let(meeting, "substatus" , Completed, true),
                        Finish(meeting)
                )
        }

        /**		Only if the meeting isn't closed and exists a minutes	*/

        override def permitted(implicit state: State) = Some(
                (meeting.substatus.get == Scheduled || meeting.substatus.get == Rescheduled)
                    &&
                (meeting.minutes.size > 0)
            )
    }
    
    implicit val CloseMeeting = builder[CloseMeeting]

	/**		Cancels a meeting	*/

    trait CancelMeeting extends SpeechAct {
	    type This = CancelMeeting
	    type Substatus = Nothing

	    /**		The context is a meeting	*/

	    type Context = Project

	    /**		The performer is the moderator	*/

	    type Performer = Moderator
	    type Addressee = Nothing

	    /**		The meeting that will be cancelled	*/

	    val old : Option[$[Meeting]]

	    def meeting = old.get

	    override def purpose(implicit state: State) = {
	            Sequence(
	                    Let(meeting, "substatus" , Undone, true),
	                    Finish(meeting)
	            )
	    }

	    /**		Only if the meeting isn't closed and exists a reason	*/

        override def permitted(implicit state: State) = Some(
                (meeting.substatus.get == Scheduled || meeting.substatus.get == Rescheduled)
                    &&
                (meeting.reason.length > 0)
            )
    }
       
    implicit val CancelMeeting = builder[CancelMeeting]
	
}