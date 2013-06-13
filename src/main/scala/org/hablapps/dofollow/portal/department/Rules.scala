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
import org.hablapps.speech

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.project._
import org.hablapps.dofollow.portal.project.task._

trait Rules{ this: speech.Program 
	with State
	with task.State
	with meeting.State
	with discussion.State
	with portal.State
	with administration.State =>


	/**		It's not allowed operators with the same code or with the admin code	*/

	authorised {
		case Play2(operator: Updatable[Operator], department: $[Department]) if operator.isA[Operator] => implicit state => 
			val o: Operator = operator.value
	    	!department.portal.departments.flatMap {
	    		case department2: $[Department] => department2.member
	    	}.exists(_.name.get == o.name.get) &&
	    		o.name.get != "" &&
	    		o.name.get != department.portal.administration.admin.name.get
	}

	/**		Abandons roles of an operator	
	*
	*	@abandon Responsible 	A responsable is abandoned when his top is abandoned
	*	@abandon Attendee 		An attendee is abandoned when his top is abandoned
	*	@abandon Invitee	 	An invitee is abandoned when his top is abandoned
	*	@abandon Issuer		 	An issuer is abandoned when his top is abandoned
	*	@abandon Receiver	 	A receiver is abandoned when his top is abandoned
	*
	*/

    when {
        case _Set(op: $[Operator]@unchecked, Operator._status, ABANDONED, true) if op.isA[Operator] =>
            For(op.role){
                case rol: $[Responsible] if rol.status.get == PLAYING && rol.isA[Responsible] => Abandon(rol)
                case rol: $[Attendee] if rol.status.get == PLAYING && rol.isA[Attendee] => Abandon(rol)
                case rol: $[Invitee] if rol.status.get == PLAYING && rol.isA[Invitee] => Abandon(rol)
                case rol: $[Issuer] if rol.status.get == PLAYING && rol.isA[Issuer] => Abandon(rol)
                case rol: $[Receiver] if rol.status.get == PLAYING && rol.isA[Receiver] => Abandon(rol)
                case _ => ActionId()
            }
      }
} 
