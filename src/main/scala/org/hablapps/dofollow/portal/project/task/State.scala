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

package org.hablapps.dofollow.portal.project.task

import org.hablapps.updatable._
import org.hablapps.react._
import org.hablapps.speech

import org.joda.time._

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.project._


trait State{ this: speech.Program
	with portal.State
	with project.State
	with administration.State
	with discussion.State
	with department.State =>

	/**		A task is the item of a specific project	*/

	trait Task extends Interaction {
		type This = Task
		type Substatus = TaskState
		type ContextCol[x] = Set[x]

		/**		There are three types of contexts
		*
		*		@context Project 		The project of the task
		*		@context Department 	The department of the task
		*		@context Task 			This task has dependencies of other tasks
		*	
		*/

		type Context = Interaction  {type Subinteraction >: Task} // Project, Department, Task
		type SubinteractionCol[x] = Set[x]

		/**	 	There are two types of subinteractions
		*
		*		@subinteraction 	Task 		Dependent tasks of this task
		*		@subinteraction 	Discussion 	Interaction with the comments about this task
		*
		*/

		type Subinteraction = Interaction
		type MemberCol[x] = Set[x]

		/**		The member of a task is the responsible of this task	*/

		type Member = Responsible
		type EnvironmentCol[x] = Set[x]

		/**		The environment of a task are the annexes	*/

		type Environment = Annex
		type ActionCol[x] = Set[x]

		/**		There are many actions can be performed in a task	
		*
		*		@action 	ScheduleTask 			Changes the date of a task
		*		@action 	InitiateTask 			Starts a task
		*		@action 	Comment 				Creates a comment
		*		@action 	AssignResponsible 		The Admin converts an operator in the responsible of a task
		*		@action 	CreateAnnex				Creates a new annex
		*		@action 	DestroyAnnex 			Destroys an annex
		*		@action 	JoinResponsible 		An operator converts himself in the responsible of a task
		* 		@action 	JoinResponsibleAdmin	The admin converts humself in the responsible of a task
		* 		@action 	LeaveResponsible 		A responsable abandons the role of responsible
		* 		@action 	FireResponsible 		The Admin fires a responsible of a task
		*		@action 	SetUpDiscussion 		Initiates a messaging module
		*
		*/

		type Action = SocialAction   

		/** Name of the task
		*
		* @kind input
		*
		*/

		val taskName:String

		/** Description of the task
		*
		* @kind input
		*
		*/

		val description:String

		/** Possible solution of the task
		*
		* @kind input
		*
		*/

		val solution:String

		/** Final estimated date of the task
		*
		* @kind input
		*
		*/

		val estimatedDate:List[Long]
		
		/** Reasons of the scheduling
		*
		* @kind input
		*
		*/

		val reason:List[String]

		/** Real final date of the task
		*
		* @kind local
		*
		*/

		val endDate:Long

		/** Deadline of the task
		*
		* @kind input
		*
		*/

		val deadline: Option[Long]

		/** Launch type of the task (A - Automatically, M - Manually)
		*
		* @kind local
		*
		*/

		val launchType:String

		/** Duration of the task
		*
		* @kind input
		*
		*/

		val duration:Int

		/** Starting date
		*
		* @kind local
		*
		*/

		val startDate:Long



		/** Starting time of a time-dependence
		*
		* @kind local
		*
		*/

		val waitingDate: Option[Long]

		/** Task of a time-dependence
		*
		* @kind input
		*
		*/

		val temporalDependence: Option[$[Task]]

		/** Waiting time of a time-dependence
		*
		* @kind input
		*
		*/

		val temporalDuration: Option[Int]

		/**		Returns if the normal dependencies are completed	*/

		def normalDependencies(implicit state: State) = predecessor.forall(t => t.substatus.get == Finished || t.substatus.get == Cancelled)

		/**		Returns if the temporal dependencies are completed	*/

		def temporalDependencies(implicit state: State) = {
			if(temporalDependence.isDefined && temporalDuration.isDefined)
				if(waitingDate.isDefined)
					waitingDate.get < now
				else
					if(temporalDependence.get.substatus.get != Cancelled)
						false
					else
						true
			else
				true
		}

		def contextDependenciesChecks(implicit state: State) = 
			if (predecessor.isEmpty)
				true
			else
				predecessor.forall(_.deadline.isDefined)

		def temporalDependenciesChecks(implicit state: State) = 
			if(temporalDependence.isDefined && temporalDuration.isDefined)
				if(temporalDependence.get.deadline.isDefined)
					true
				else
					false
			else
				true


		def project = context.alias[Project].head
		def annexes = environment.alias[Annex]
		def responsible = member.filter(_.status.head == PLAYING).head 
		def predecessor = context.alias[Task]  
		def childs = subinteraction.alias[Task]  
		def department = context.alias[Department].head  
		def discussion = subinteraction.alias[Discussion].head  
	}
	
	implicit val Task = builder[Task]

	trait TaskState extends EntityStatus
	case object Waiting extends TaskState
	case object Executing extends TaskState
	case object Delayed extends TaskState
	case object Finished extends TaskState
	case object Cancelled extends TaskState

	trait ZombieTaskState extends EntityStatus
	case object ZombieTask extends ZombieTaskState

	/** 	@declarer When the substatus of the task isn't Completed or Cancelled	*/

	declarer[Admin].of[Task](Task._taskName)
	    .permitted {
	      case (admin, task1, _) => implicit state => 
	      	Some(task1.substatus.get != Finished && task1.substatus.get != Cancelled)
	    }

    /** 	@declarer When the substatus of the task isn't Completed or Cancelled	*/

	declarer[Admin].of[Task](Task._description)
	    .permitted {
	      case (admin, task1, _) => implicit state => 
	      	Some(task1.substatus.get != Finished && task1.substatus.get != Cancelled)
	    }

    /** 	
    *
    *	@declarer When the performer is the responsible of the task or the Admin.
    *	Also, the task has to be executed.
    *
	*/

	declarer[Agent].of[Task](Task._solution)
	    .empowered {
	      case (agente, task1, _) => implicit state => 
	      	agente match {
	      		case x: $[Responsible] if x.isA[Responsible] =>
	      			x == task1.responsible 
      			case x: $[Admin] if x.isA[Admin] =>
      				true
	      	}
	      	
	    }
	    .permitted {
	      case (agente, task1, _) => implicit state => 
	      	Some(task1.substatus.get == Executing)
	    }

    /**		The responsible agent of a task	*/

	trait Responsible extends Agent {
		type This = Responsible
		type Substatus = ZombieTaskState

		/**		The context is a task	*/

		type Context = Task
		type PlayerCol[x] = Set[x]

		/**		There are two possible agents
		*
		*		@player 	Operator 	Operator of a department
		*		@player 	Admin 		Admin of the application
		*
		*/

		type Player = Agent {type Role >: Responsible}
		type RoleCol[x] = Traversable[x]

		/**		The role are issuer	*/

		type Role = Issuer
		type PerformCol[x] = Set[x]

		/**		A Responsible can perform many actions
		*
		*		@perform 	ScheduleTask 		Changes the date of a task
		*		@perform 	Comment 			Creates a comment
		*		@perform 	AssignResponsible 	The Admin converts an operator in the responsible of a task
		*		@perform 	CreateAnnex			Creates a new annex
		*		@perform 	DestroyAnnex 		Destroys an annex
		* 		@perform 	LeaveResponsible 	A responsable abandons the role of responsible
		*		@perform 	SetUpDiscussion 	Initiates a discussion
		*		@perform    CloseTask 			Finishes a task
		*		@perform 	Reply				Replies a comment in a discussíon
		*
		*/

		type Perform = SocialAction 

		def task = context.head
		def operator = player.alias[Operator].head
		def admin = player.alias[Admin].head
	}
	
	implicit val Responsible = builder[Responsible]

	/**		It's the documentation of a task	*/

	trait Annex extends Resource {
		type This = Annex
		type Substatus = ZombieTaskState

		/**		The context is a task	*/

		type Context = Task
		type CreatorCol[x] = Set[x]

		/**		The creator can be a Responsible or the Admin
		*
		* 		@owner Responsible 	Responsible agent of a task
		*		@owner Admin 		Admin of the application
		*
		*/

		type Creator = Agent
		type OwnerCol[x] = Set[x]

		/**		The owner can be a Responsible or the Admin
		*
		* 		@owner Responsible 	Responsible agent of a task
		*		@owner Admin 		Admin of the application
		*
		*/

		type Owner = Agent

		/** Name of the file
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
	
	implicit val Annex = builder[Annex]

	/**		Changes the date of a task	*/

	trait ScheduleTask extends SpeechAct {
		type This = ScheduleTask
		type Substatus = Nothing

		/**		The context is a task	*/

		type Context = Task

		/**		The performer can be a Responsible or the Admin
		*
		* 		@performer Responsible 	Responsible agent of a task
		*		@performer Admin 		Admin of the application
		*
		*/

		type Performer = Agent {type Perform >: ScheduleTask}
		type Addressee = Nothing

		/** Date of the new scheduling
		*
		* @kind input
		*
		*/

		val date:Long

		/** Reason of the new scheduling
		*
		* @kind input
		*
		*/

		val reason:String

		def task = context.head

		/** 	Updates the reason and the date of the new scheduling	*/

		override def purpose(implicit state: State) = {
			if(date != 0 && date > now && reason != "")
				Sequence(
					Let(task, "reason", reason, true),
					Let(task, "estimatedDate", date, true),
					Let(task, "deadline", date, true)
				)
			else
				ActionId()
		}

		/** 	The responsible of the task and the admin	*/

		override def empowered(implicit state: State) = 
			performer.head match {
				case x: $[Responsible] if x.isA[Responsible] => task == x.task
				case x: $[Admin] if x.isA[Admin] => true
			}

		/**		The task has to be executed or delayed	*/

		override def permitted(implicit state: State) = 
			Some((task.substatus.get == Executing || task.substatus.get == Delayed) && reason != "")
	}
	
	implicit val ScheduleTask = builder[ScheduleTask]

	/**		Starts a task	*/
	
	trait InitiateTask extends SpeechAct {
		type This = InitiateTask
		type Substatus = Nothing

		/**		The context is a task	*/

		type Context = Task

		/**		The performer is the Admin	*/

		type Performer = Admin
		type Addressee = Nothing

		def task = context.head

		/**		Updates the type of launch, the starting date, the deadline and the substatus of the task	*/

		override def purpose(implicit state: State) = {
			Sequence(
				LetWholeExtension(task, "launchType" , "M"),
				LetWholeExtension(task, "startDate", now),
				Let(task, "deadline" , addBusinessDays(now, task.duration), true),
				Let(task, "substatus", Executing, true)
			)
		}

		/**		Only if the task is pending to executed	*/

		override def permitted(implicit state: State) = 
			Some(task.substatus == Some(Waiting))
	}
	
	implicit val InitiateTask = builder[InitiateTask]

	/**		Creates a new comment	*/

	trait Comment extends SpeechAct {
		type This = Comment
		type Substatus = Nothing

		/**		The context is a task	*/

		type Context = Task

		/**		The performer can be anyone	*/	

		type Performer = Agent {type Perform >: Comment}
		type Addressee = Nothing

		/** Date of the commment
		*
		* @kind local
		*
		*/

		val date:Long


		def comentarista = performer.head
		def task = context.head

		/**		The task has to be executed	*/

		override def permitted(implicit state: State) = Some(task.substatus.get == Executing)
	}

	implicit val Comment = builder[Comment]

	/**		The Admin converts an operator in the responsible of a task 	*/
	
	trait AssignResponsible extends Assign {
		type This = AssignResponsible
		type Substatus = Nothing

		/**		The context is a task	*/

		type Context = Task

		/**		The performer can be a Responsible or the Admin
		*
		* 		@performer Responsible 	Responsible agent of a task
		*		@performer Admin 		Admin of the application
		*
		*/		

		type Performer = Agent {type Perform >: AssignResponsible}
		type Addressee = Nothing
		type New = Responsible

		/**		The player is an operator	*/

		type Player = Operator

		def adminTask = performer.head
		def task = context.head
		def operator = player.head

		/** 	The responsible of the task and the admin	*/

		override def empowered(implicit state: State):Boolean = adminTask match {
			case x: $[Responsible] if x.isA[Responsible] => x.task == task && operator.department == task.department
			case x: $[Admin] if x.isA[Admin] => operator.department == task.department
			case _ => false
		} 

		/** 	The new responsible has to belong to department of the task.
		*
		*	If it's a task of the admin department the new responsible has to be the project admin.	
		*
		*/

		override def permitted(implicit state: State) = 
			if (operator.department.name.get != adminDepartment.toString)
			    Some(task.substatus.get == Executing)
			else
				Some((task.substatus.get == Executing) && (task.project.projectAdmin == operator.name.get))

		def EvidenceNew = implicitly[Evidence[New]]
		def BuilderNew: Builder[New] = implicitly[Builder[New]]
		def EvidencePlayer: Evidence[Operator] = implicitly[Evidence[Operator]]
		def BuilderPlayer: Builder[Operator] = implicitly[Builder[Operator]]
	}
	
	implicit val AssignResponsible = builder[AssignResponsible]

	/**		Creates a new annex	*/

	trait CreateAnnex extends Create {
		type This = CreateAnnex
		type Substatus = Nothing

		/**		The context is a task	*/

		type Context = Task

		/**		The performer can be a Responsible or the Admin
		*
		* 		@performer Responsible 	Responsible agent of a task
		*		@performer Admin 		Admin of the application
		*
		*/

		type Performer = Agent {type Perform >: CreateAnnex}
		type Addressee = Nothing
		type New = Annex

		def creator = performer.head
		def task = context.head

		/** 	The responsible of the task and the admin	*/

		override def empowered(implicit state: State) = creator match {
			case x: $[Responsible] if x.isA[Responsible] => task == x.task
			case x: $[Admin] if x.isA[Admin] => true
			case _ => false
		}

		/**		The task has to be executed	*/

		override def permitted(implicit state: State) = Some(task.substatus.get == Executing)
	
		def EvidenceNew = implicitly[Evidence[New]]
	    def BuilderNew:Builder[Annex] = implicitly[Builder[Annex]]
	}
	
	implicit val CreateAnnex = builder[CreateAnnex]
	
	/**		Destroys an annex	*/

	trait DestroyAnnex extends Destroy {
		type This = DestroyAnnex
		type Substatus = Nothing

		/**		The context is a task	*/

		type Context = Task

		/**		The performer can be a Responsible or the Admin
		*
		* 		@performer Responsible 	Responsible agent of a task
		*		@performer Admin 		Admin of the application
		*
		*/

		type Performer = Agent {type Perform >: DestroyAnnex}
		type Addressee = Nothing
		type Resource = Annex

		def destroyer = performer.head
		def task = context.head

		/** 	The responsible of the task and the admin	*/

		override def empowered(implicit state: State):Boolean = destroyer match {
			case x: $[Responsible] if x.isA[Responsible] => task == x.task
			case x: $[Admin] if x.isA[Admin] => true
			case _ => false
		}

		/**		The task has to be executed	*/

		override def permitted(implicit state: State) = Some(task.substatus.get == Executing)		
	}
	
	implicit val DestroyAnnex = builder[DestroyAnnex]

	/**		An operator converts himself in the responsible of a task	*/
		
	trait JoinResponsible extends Join {
		type This = JoinResponsible
		type Substatus = Nothing
		
		/**		The context is a task	*/

		type Context = Task

		/**		The performer is an operator	*/

		type Performer = Operator
		type Addressee = Nothing
		type New = Responsible

		def task = context.head
		def operator = performer.head

		/**		The new responsible has to be Ready and the task hasn't to have responsibles	*/

		override def empowered(implicit state: State):Boolean = 
			operator.substatus.get != Hidden && operator.department == task.department && !task.member.exists(_.status.get == PLAYING)

		/**		The task has to be executed	*/

		override def permitted(implicit state: State) = Some(task.substatus.get == Executing)
	
		def NewE = implicitly[Evidence[New]]
	    def BuilderE: Builder[New] = Responsible
	}
	
	implicit val JoinResponsible = builder[JoinResponsible]

	/**		A responsable abandons the role of responsible	*/

	trait LeaveResponsible extends Leave {
		type This = LeaveResponsible
		type Substatus = Nothing
		
		/**		The context is a task	*/

		type Context = Task

		/**		The performer is a responsible	*/

		type Performer = Responsible
		type Addressee = Nothing
	}
	
	implicit val LeaveResponsible = builder[LeaveResponsible]
	
	/**		The admin converts humself in the responsible of a task	*/

	trait JoinResponsibleAdmin extends Join {
		type This = JoinResponsibleAdmin
		type Substatus = Nothing
		
		/**		The context is a task	*/

		type Context = Task

		/**		The performer is the admin of the application	*/

		type Performer = Admin
		type Addressee = Nothing
		type New = Responsible

		def task = context.head

		/**		The task has to be executed and the task hasn't to have responsibles	*/

		override def permitted(implicit state: State) = Some(task.substatus.get == Executing && !task.member.exists(_.status.get == PLAYING))
	
		def NewE = implicitly[Evidence[New]]
	    def BuilderE: Builder[New] = Responsible
	}
	
	implicit val JoinResponsibleAdmin = builder[JoinResponsibleAdmin]

	/**		The Admin fires a responsible of a task	*/	

	trait FireResponsible extends Fire {
		type This = FireResponsible
		type Substatus = Nothing
		
		/**		The context is a task	*/

		type Context = Task

		/**		The performer is the admin of the application	*/

		type Performer = Admin
		type Addressee = Nothing
		type Role = Responsible
	}
	
	implicit val FireResponsible = builder[FireResponsible]

	/**		Initiates a messaging module	*/

	trait SetUpDiscussion extends SetUp {
		type This = SetUpDiscussion
		type Substatus = Nothing
		
		/**		The context is a task	*/

		type Context = Task

		/**		The performer can be anyone	*/	

		type Performer = Agent {type Perform >: SetUpDiscussion}
		type Addressee = Agent
		type New = Discussion

		/** Code of the receptor
		*
		* @kind input
		*
		*/

		val user:String
		
		/** Department of the receptor
		*
		* @kind input
		*
		*/

		val department:String

		def task = context.head
		def issuer = performer.head
	
	    def NewE = implicitly[Evidence[Discussion]]
	    def BuilderE: Builder[Discussion] = Discussion

	    /**		The task has to be executed	*/

		override def permitted(implicit state: State) = Some(task.substatus.get == Executing)
	}
	
	implicit val SetUpDiscussion = builder[SetUpDiscussion]
	

}