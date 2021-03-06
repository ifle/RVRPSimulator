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
package vrpsim.core.model.structure.occasionaldriver;

import java.util.List;
import java.util.Observable;

import vrpsim.core.model.VRPSimulationModelElementParameters;
import vrpsim.core.model.behaviour.activities.util.TimeCalculationInformationContainer;
import vrpsim.core.model.behaviour.tour.ITour;
import vrpsim.core.model.events.IEvent;
import vrpsim.core.model.events.IEventType;
import vrpsim.core.model.network.IVRPSimulationModelNetworkElement;
import vrpsim.core.model.solution.Order;
import vrpsim.core.model.solution.PublicOrderPlatform;
import vrpsim.core.model.structure.VRPSimulationModelStructureElementParameters;
import vrpsim.core.model.structure.util.storage.DefaultStorageManager;
import vrpsim.core.model.util.exceptions.EventException;
import vrpsim.core.model.util.functions.ITimeFunction;
import vrpsim.core.simulator.EventListService;
import vrpsim.core.simulator.IClock;
import vrpsim.core.simulator.ITime;

public class OccasionalDriver extends AbstractOccasionalDriver {

	private ITour myDailyBehaviour;
	private Double averageSpeed;

	public OccasionalDriver(final VRPSimulationModelElementParameters vrpSimulationModelElementParameters,
			final VRPSimulationModelStructureElementParameters vrpSimulationModelStructureElementParameters,
			final DefaultStorageManager storageManager, ITour myDailyBehaviour, Double averageSpeed) {
		super(vrpSimulationModelElementParameters, vrpSimulationModelStructureElementParameters, storageManager);

		this.myDailyBehaviour = myDailyBehaviour;
		this.averageSpeed = averageSpeed;
	}

	@Override
	public List<IEventType> getAllEventTypes() {
		return this.myDailyBehaviour.getAllEventTypes();
	}

	@Override
	public List<IEvent> getInitialEvents(IClock clock) {
		return this.myDailyBehaviour.getInitialEvents(clock);
	}

	@Override
	public List<IEvent> processEvent(IEvent event, IClock clock, EventListService eventListAnalyzer)
			throws EventException {
		return this.myDailyBehaviour.processEvent(event, clock, eventListAnalyzer);
	}

	@Override
	public Double getAverageSpeed() {
		return this.averageSpeed;
	}

	@Override
	public void update(Observable o, Object arg) {
		this.logger.info("Got informed about change of OrderBord.");

		// TODO Simulation time?

		PublicOrderPlatform orderBord = (PublicOrderPlatform) o;
		if (orderBord.confirmOrder((Order) arg, this)) {
			// TODO

		}

	}

	@Override
	public void setCurrentPlace(IVRPSimulationModelNetworkElement networkElement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ITime getServiceTime(TimeCalculationInformationContainer container, IClock clock) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ITimeFunction getServiceTimeFunction() {
		// TODO Auto-generated method stub
		return null;
	}

}
