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
package vrpsim.core.model;

import java.util.ArrayList;
import java.util.List;

import vrpsim.core.model.behaviour.BehaviourService;
import vrpsim.core.model.behaviour.tour.ITour;
import vrpsim.core.model.network.Network;
import vrpsim.core.model.network.NetworkService;
import vrpsim.core.model.solution.IAmInterestedInPubishedOrders;
import vrpsim.core.model.solution.SolutionManager;
import vrpsim.core.model.structure.Structure;
import vrpsim.core.model.structure.StructureService;
import vrpsim.core.model.structure.customer.ICustomer;
import vrpsim.core.model.structure.depot.IDepot;
import vrpsim.core.model.structure.occasionaldriver.OccasionalDriver;
import vrpsim.core.simulator.EventListService;

/**
 * @date 03.02.2016
 * @author thomas.mayer@unibw.de
 *
 */
public class VRPSimulationModel {

	private final VRPSimulationModelParameters vrpSimulationModelParameters;

	private final Structure structure;
	private final Network network;

	private SolutionManager solutionManager;

	public VRPSimulationModel(final VRPSimulationModelParameters vrpSimulationModelParameters,
			final Structure structure, final Network network) {

		this.vrpSimulationModelParameters = vrpSimulationModelParameters;

		this.structure = structure;
		this.network = network;
	}
	
	public StructureService getStructureService() {
		return this.structure.getStructureService();
	}
	
	public NetworkService getNetworkService() {
		return this.network.getNetworkService();
	}
	
	public BehaviourService getBehaviourService() {
		return this.solutionManager.getBehaviourFromInitialBehaviourProvider().getBehaviourService();
	}

	/**
	 * Sets the {@link SolutionManager} to the model in general a
	 * {@link VRPSimulationModel} can describe no solution, e.g. only the
	 * arrival at the {@link IDepot}, {@link ICustomer} consumption can be
	 * simulated.
	 * 
	 * @param solutionManager
	 */
	public void setSolutionManager(SolutionManager solutionManager) {
		this.solutionManager = solutionManager;
	}

	/**
	 * Publishes the {@link EventListService} to the {@link SolutionManager},
	 * which the order manager needs e.g. to remove {@link ITour}s from the list
	 * or deploy new {@link ITour}s. Also publishes the {@link EventListService}
	 * to all {@link OccasionalDriver}s. If no {@link SolutionManager} exists,
	 * only the elements from {@link Structure} and {@link Network} are
	 * returned.
	 * 
	 * Returns all existing {@link IVRPSimulationElement}.
	 * 
	 * @param eventListInterface
	 * @return
	 */
	public List<IVRPSimulationElement> initalizeAndReturnSimulationElements(EventListService eventListInterface) {

		List<IVRPSimulationElement> allElements = new ArrayList<>();
		allElements.addAll(this.structure.getAllSimulationElements());
		allElements.addAll(this.network.getAllSimulationElements());

		if (this.solutionManager != null) {
			allElements.addAll(this.solutionManager.initalizeAndReturnSolutionBehaviourSimulationElements(
					eventListInterface, structure.getStructureService(), network.getNetworkService()));
			
			if (this.solutionManager.getDynamicBehaviourProvider() != null) {
				for (IVRPSimulationElement element : allElements) {
					if(element instanceof IAmInterestedInPubishedOrders) {
						((IAmInterestedInPubishedOrders)element).registerToObserve(this.solutionManager.getDynamicBehaviourProvider().getOrderBord());
						((IAmInterestedInPubishedOrders)element).registerServices(eventListInterface, structure.getStructureService(), network.getNetworkService());
					}
				}
			}
		}

		return allElements;
	}

	public VRPSimulationModelParameters getVrpSimulationModelParameters() {
		return vrpSimulationModelParameters;
	}

	public Network getNetwork() {
		return network;
	}

	public Structure getStructure() {
		return structure;
	}

	public SolutionManager getSolutionManager() {
		return this.solutionManager;
	}

}
