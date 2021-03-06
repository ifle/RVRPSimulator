/**
 * Copyright © 2016 Thomas Mayer (thomas.mayer@unibw.de)
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
package vrpsim.core.model.events;

import vrpsim.core.model.solution.Order;
import vrpsim.core.simulator.ITime;

/**
 * 
 * 
 * @author thomas.mayer@unibw.de
 */
public class OrderEvent implements IEvent {

	private final IEventOwner owner;
	private final IEventType type;
	private final Integer priority;
	private final ITime timeTillOccurence;

	private Order order;
	private ITime simulationtimeTillOccurence;

	public OrderEvent(IEventOwner owner, IEventType type, Integer priority, ITime timeTillOccurence, Order order) {
		this.owner = owner;
		this.type = type;
		this.priority = priority;
		this.timeTillOccurence = timeTillOccurence;
		this.order = order;
	}
	
	public Order getOrder() {
		return order;
	}

	@Override
	public ITime getTimeTillOccurrence() {
		return this.timeTillOccurence;
	}

	@Override
	public void setSimulationTimeOfOccurrence(ITime time) {
		this.simulationtimeTillOccurence = time;
	}

	@Override
	public ITime getSimulationTimeOfOccurence() {
		return this.simulationtimeTillOccurence;
	}

	@Override
	public Integer getPriority() {
		return this.priority;
	}

	@Override
	public IEventType getType() {
		return this.type;
	}

	@Override
	public IEventOwner getOwner() {
		return this.owner;
	}

}