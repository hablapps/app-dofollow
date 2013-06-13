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
import org.hablapps.speech

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.project._

trait Rules{ this: speech.Program 
	with department.State
	with task.State
	with meeting.State
	with administration.State
	with State 
	with Actions =>

	/**		When a  new message is created it's necessary to update the date of the new message	*/

    when {
        case _Set(reply: $[Reply], Reply._status, PERFORMED, true) if reply.isA[Reply] =>
        	LetWholeExtension(reply, "date", nowWithMillis)
      }

    /**		When a  new message is created it's necessary to play new roles and update dates	
    *
    *		@play 	Issuer 		When a new discussion is initiated.
    *
    */  

	when {
	    case Performed(setUp: SetUpDiscussion) => implicit state =>
	    	Sequence(
	    		setUp.issuer.top.head match{
	    			case x:$[Operator] if x.isA[Operator] => Play3(Issuer(), x, setUp._new_entity.get)
	    			case x:$[Admin] if x.isA[Admin] => Play3(Issuer(), x, setUp._new_entity.get)
	    		},
	    		PlayReceiver(setUp, setUp.task.department.portal),
	    		LetWholeExtension(setUp._new_entity.head, "date", nowWithMillis)
	     	)
	  }

	/**		When a discussion is closed it's necessary to abandon his members			
	*
	*		@abandon 	Issuer 		When the discussion is closed
	*		@abandon 	Receiver 	When the discussion is closed
	*
	*/

   	when {
    	case _Set(m: $[Discussion], SocialEntity._status, CLOSED, true) if m.isA[Discussion] => implicit state =>
			For(m.member){
				case x: $[Issuer] if x.isA[Issuer] && x.status.head == PLAYING =>
					Abandon(x)
				case x: $[Receiver] if x.isA[Receiver] && x.status.head == PLAYING =>
					Abandon(x)
				case _ => ActionId()
    		}
   	}
} 
