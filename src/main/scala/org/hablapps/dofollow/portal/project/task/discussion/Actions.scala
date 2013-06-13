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

import org.hablapps.dofollow._
import org.hablapps.dofollow.portal._
import org.hablapps.dofollow.portal.project._

trait Actions { this: speech.Program 
	with portal.State
	with administration.State
	with department.State
	with task.State
	with State =>

	/**	Creates a new receiver
	*
	*	@play 	Receiver 	When a new discussion is initiated
	*
	*/	

	case class PlayReceiver(setUp: SetUpDiscussion, portal1: $[Portal]) extends DefinedAction( implicit state =>
		if(setUp.user == adminCod) {
				Play3(Receiver(), portal1.administration.admin, setUp._new_entity.get)
			} else {
				Play3(Receiver(), 
					portal1.departments.filter(_.name.get == setUp.department).head.operators.filter(_.name.get == setUp.user).head,
					setUp._new_entity.get)
			}		
	)

} 
