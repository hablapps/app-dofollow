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
import org.hablapps.speech

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.project.task._

trait Rules{ this: speech.Program 
	with State
	with department.State
    with portal.State
    with project.State
    with administration.State
    with discussion.State =>


    /**    When an attendee is played, the invitee is abandoned     
    *
    *       @abandon    Invitee    When the operator confirms attendance
    *
    */

    when {
        case New(attendee: $[Attendee], _: Attendee) if attendee.isA[Attendee] => implicit state =>
        	Abandon(attendee.meeting.invitees.filter(_.operator == attendee.operator).filter(_.status.head == PLAYING).head) 
      }


    /**    When a meeting is closed it's necessary to abandon roles and destroy resources
    *
    *       @abandon    Moderator   When the meeting is closed
    *       @abandon    Attendee   When the meeting is closed
    *       @abandon    Invitee    When the meeting is closed
    *       @delete     Minutes        When the meeting is closed
    *
    */

    when {
        case _Set(meeting: $[Meeting], substatus, Completed, true) =>
            Sequence(
                For(meeting.member){
                    case m: $[Moderator] if m.isA[Moderator] && m.status.head == PLAYING =>
                        Sequence(
                            Let(m, "substatus", ZombieMeeting, true),
                            Abandon(m)
                        )
                    case a: $[Attendee] if a.isA[Attendee] && a.status.head == PLAYING =>
                        Sequence(
                            Let(a, "substatus", ZombieMeeting, true),
                            Abandon(a)
                        )
                    case i: $[Invitee] if i.isA[Invitee] && i.status.head == PLAYING =>
                        Sequence(
                            Let(i, "substatus", ZombieMeeting, true),
                            Abandon(i)
                        )
                    case _ => ActionId()
                },
                For(meeting.environment){
                    case r: $[Minutes] if r.status.head == CREATED => Sequence(
                        Let(r, "substatus", ZombieMeeting, true),
                        DeleteR(r)
                    )
                    case _ => ActionId()
                }
            )
    } 
 

    /**    When a minutes is created it's necessary to update the meeting date and the creator    */

    when {
        case Performed(create: CreateMinutes) if create.isA[CreateMinutes] => implicit state =>
            Sequence(
                Let(create._new_entity.head, "creator", create.moderator, true),
                LetWholeExtension(create._new_entity.head, "uploadDate", now)
            )
    }

    /**    When an attendee is abandoned it's necessary to abandon his roles     
    *
    *       @abandon    Issuer      When his top role is abandoned
    *       @abandon    Receiver    When his top role is abandoned
    *
    */

    when {
        case _Set(attendee: $[Attendee], Attendee._status, ABANDONED, true) if attendee.isA[Attendee] =>
            For(attendee.role){
                case rol: $[Issuer] if rol.status.get == PLAYING && rol.isA[Issuer] => Abandon(rol)
                case rol: $[Receiver] if rol.status.get == PLAYING && rol.isA[Receiver] => Abandon(rol)
                case _ => ActionId()
            }
      }

    /**    When an invitee is abandoned it's necessary to abandon his roles     
    *
    *       @abandon    Issuer      When his top role is abandoned
    *       @abandon    Receiver    When his top role is abandoned
    *
    */

    when {
        case _Set(invitee: $[Invitee], Invitee._status, ABANDONED, true) if invitee.isA[Invitee] =>
            For(invitee.role){
                case rol: $[Issuer] if rol.status.get == PLAYING && rol.isA[Issuer] => Abandon(rol)
                case rol: $[Receiver] if rol.status.get == PLAYING && rol.isA[Receiver] => Abandon(rol)
                case _ => ActionId()
            }
      }

    /**    When the meeting date is changed it's necessary to change attributes and roles
    *
    *   @play       Invitee    When a meeting has a new planning
    *   @abandon    Attendee   When a meeting has a new planning
    *     
    */

    when {
        case _Set(meeting1: $[Meeting], Meeting._date, _, _) if meeting1.isA[Meeting] => implicit state =>
            Sequence(
                Let(meeting1, "substatus" , Rescheduled, true),
                For(meeting1.attendees) {
                    case attendee if attendee.status.head == PLAYING =>
                        Sequence(
                            Play3(Invitee(), attendee.operator, meeting1),  
                            Abandon(attendee) 
                        )
                    case _ => ActionId()
                }
            )
      }

    /**    When a meeting is cancelled it's necessary to abandon roles
    *
    *       @abandon    Moderator   When the meeting is cancelled
    *       @abandon    Attendee   When the meeting is cancelled
    *       @abandon    Invitee    When the meeting is cancelled
    *
    */

    when {
        case _Set(meeting: $[Meeting], SocialEntity._substatus, Undone, true) if meeting.isA[Meeting] => implicit state =>
            Sequence(
                For(meeting.attendees) {
                    case attendee if attendee.status.get == PLAYING => 
                        Abandon(attendee)
                    case _ => ActionId()
                },
                For(meeting.invitees) {
                    case invitee if invitee.status.get == PLAYING => 
                        Abandon(invitee)
                    case _ => ActionId()
                },
                Abandon(meeting.moderator)
            )
    }    
}