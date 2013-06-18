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
import org.hablapps.speech

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.department._
import org.hablapps.dofollow.portal.administration._
import org.hablapps.dofollow.portal.administration.projectModel._
import org.hablapps.dofollow.portal.project._

trait Actions { this: speech.Program 
      with State 
      with department.State 
      with task.State
      with project.State
      with projectModel.State
      with taskModel.State
      with administration.State =>

  /**   Loads all the models of departments
  * 
  *     @initiate Department All the departments are initiated when a portal is initiated.
  *
  */

	case class LoadDepartments(p: $[Portal]) extends DefinedAction( implicit state =>
    For(departmentsMap) {
      case a => Initiate2(Department(_name = Some(a._2._1), _departmentName = a._2._2), p)
    }
	)

  /**   Loads all the models of projects (Data are in a csv file). Then, model of tasks are created.
  *
  *     @initiate ProjectModel  ProjectModel is initiated when a portal is initiated
  *
  */

  case class LoadProjectModels(administration: $[Administration]) extends DefinedAction( implicit state =>
    For(projectsMap) {
      case project => 
        for {
          p <- Initiate2(ProjectModel( _name = Some(project._2._1),  
                            _projectName = project._2._2), administration)
          _ <- LoadTaskModels(p)
        }yield()
    }
  )

  /** Loads all the models of task of a project.
  *
  *   @initiate TaskModel   TaskModel is initiated when his ProjectModel is initiated
  *
  */

  case class LoadTaskModels(project: $[ProjectModel]) extends DefinedAction( implicit state =>
    For(projectsMap(project.name.get.toInt)._5) {
      case task =>
          Initiate2(TaskModel(_name = Some(task._1),
                        _taskName = task._2,
                        _description = task._3,
                        _dependencies = if (task._4 == "") Set() else task._4.split(",").toSet,
                        _duration = task._5.toInt,
                        _department = task._8,
                        _temporalDependence = if (task._6 != "") Some(task._6) else None,
                        _temporalDuration = if (task._7 != "") Some(task._7.toInt) else None ,
                        _waitingDate = None), project)
    }
  )  

  /** Creates a operator in the Admin Department
  *
  * @play Operator Operator is played in Admin Department when a Project is 
  *       created and the adminCode isn't belong to an existing operator.
  *
  */

  case class PlayProjectAdmin(codAdmin: String, adminDepartment: $[Department]) extends DefinedAction( implicit state =>
      Play2(Operator(_name = Some(codAdmin), _substatus = Some(Hidden)), adminDepartment)
  )

  /** Initiates all the tasks of a project, links these tasks with their departments and links these tasks to each other.
  *
  * @initiate  Task  A task is created when his project is initiated
  *
  */

  case class InitiateTasks(setUpProject1: SetUpProject) extends DefinedAction( implicit state =>
    Sequence(
      For(setUpProject1.portal.administration.projectModels.filter( _.name.get == setUpProject1.codProject).head.tasks){
      case t =>
        for{
          newTask          <-   Initiate2(Task(_name = t.name, _taskName = t.taskName, _description = t.description, 
                                                  _substatus = Some(Waiting), _duration = t.duration,
                                                   _temporalDuration = t.temporalDuration,
                                                  _waitingDate = t.waitingDate, _deadline = Some(0), _launchType = "A"), 
                                            setUpProject1._new_entity.head)
           _                  <- LinkTaskDepartment(newTask, setUpProject1.portal.departments.filter(_.name.get == t.department).head)
        } yield ()},
      InitiateDependencies(setUpProject1._new_entity.get, setUpProject1.codProject)      
    )

  )

  /*   Links a task with his department   */

  case class LinkTaskDepartment(task: $[Task], department: $[Department]) extends DefinedAction( implicit state =>
    Sequence(
      Let(department.subinteraction += task),
      Let(task.context += department)
    )
  )

  /* 
  *
  *  Links tasks to each other according to the dependencies of the project.
  *  Finally, if a task hasn't dependencies, this task is started automatically.
  *
  */

  case class InitiateDependencies(project : $[Project], codProject: String) extends DefinedAction( implicit state =>
    For(project.tasks){
      case newTask => implicit state =>
          val taskModel = project.portal.administration.projectModels
            .filter(_.name.get == codProject).head.tasks
            .filter(t => t.name == newTask.name && t.department == newTask.department.name.get).head
          Sequence(
            if(taskModel.dependencies.size == 0)
              if(taskModel.temporalDependence == None && taskModel.temporalDuration == None)
                StartTask(newTask)
              else
                ActionId()
            else
              For(taskModel.dependencies flatMap { case t => project.tasks.filter(_.name.get == t)}){
                case dependenceTask => LinkTasks(newTask, dependenceTask)
            },
            if(taskModel.temporalDependence.isDefined){
              val nameTask = project.portal.administration.projectModels.filter(_.name.get == codProject).head.tasks
                .filter(_.name.get == newTask.name.get).head.name.get
              Let(newTask, "temporalDependence", project.tasks.filter(_.name.get == taskModel.temporalDependence.get).head, true)
            }else
              ActionId()
          )         
    }
  )

  /* Links two tasks   */

  case class LinkTasks(task1: $[Task], task2:$[Task]) extends DefinedAction( implicit state =>
    Sequence(
      Let(task1, "context", task2, true),
      Let(task2, "subinteraction", task1, true)
    )
  )

  /*  Starts a task    */

  case class StartTask(newTask: $[Task]) extends DefinedAction( implicit state =>
    Sequence(
      implicit state => Let(newTask, "substatus" ,Executing, true),
      implicit state => LetWholeExtension(newTask, "launchType", "A")
    ) 
  )

  /* Checks that the active tasks are on time. Checks temporal dependencies too. It's necessary a batch */

  case class UpdateTaskState() extends DefinedAction( implicit state => {
    val portal1 = { val List(p: $[Portal]) = tops(state); (p) }
    Sequence(      
      For(portal1.projects) {
        case p => For(p.tasks) {
          case t => 
            if(t.substatus.get == Executing && t.deadline.get < now) 
                Let(t.substatus += Delayed)
            else
              if(t.substatus.get == Waiting && t.normalDependencies && t.temporalDependencies)
                  Let(t, "substatus", Executing, true)
              else
                ActionId()
        }
      }  
    )
  })

  /**  Register and unregister users. It's necessary a batch 
  *
  *   @abandon Operator   An Operator is abandoned when has a "B" in the csv file
  *
  */

  case class UpdateUsers(users: List[Tuple6[String, String, String, String, String, String]]) extends DefinedAction( implicit state => {
    val portal1 = { val List(p: $[Portal]) = tops(state); (p) }
      {
      val result = For(users){
        case user => implicit state =>
          For(portal1.departments) {
            case department => 
              For(department.operators) {
                case op => implicit state =>
                  if(user._6 == "B" && op.name.get == user._1)
                    Abandon(op)
                  else
                    if(user._6 == "A" && op.name.get == user._1)
                      Sequence(
                        LetWholeExtension(op, "forename" , user._2),
                        LetWholeExtension(op, "surname" , user._3 + " " + user._4),
                        LetWholeExtension(op, "email" , user._5),
                        Let(op.substatus += Enable)
                      )
                    else
                      ActionId()
            }
          }
      }
      result
      } 

  })

} 
