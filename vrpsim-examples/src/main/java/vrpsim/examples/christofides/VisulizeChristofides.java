/**
 * Copyright (C) 2016 Thomas Mayer (thomas.mayer@unibw.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vrpsim.examples.christofides;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import vrpsim.core.model.VRPSimulationModel;
import vrpsim.core.model.behaviour.Behaviour;
import vrpsim.core.model.solution.SolutionManager;
import vrpsim.core.model.util.exceptions.BehaviourException;
import vrpsim.core.model.util.exceptions.EventException;
import vrpsim.core.model.util.exceptions.NetworkException;
import vrpsim.core.model.util.exceptions.StorageException;
import vrpsim.core.model.util.exceptions.VRPArithmeticException;
import vrpsim.core.simulator.IClock;
import vrpsim.core.simulator.ITime;
import vrpsim.core.simulator.MainProgramm;
import vrpsim.examples.simple.SimpleBehaviorGenerator;
import vrpsim.examples.support.CustomerTour;
import vrpsim.examples.support.VRPREPImporter;
import vrpsim.visualization.Visualisation;

public class VisulizeChristofides extends Visualisation {

	private static String workWith = "CMT12";

	public static void main(String[] args)
			throws JAXBException, VRPArithmeticException, StorageException, NetworkException, BehaviourException, EventException, FileNotFoundException, URISyntaxException {

		MainProgramm mainProgramm = new MainProgramm();
		IClock clock = mainProgramm.getSimulationClock();
		ITime simulationEndTime = clock.getCurrentSimulationTime().createTimeFrom(100.0);

		VRPREPImporter importer = new VRPREPImporter("Storage A", "Item A", "Capacity", 100.0, 15000.0, 10000, 1);

		VRPSimulationModel model = importer
				.getSimulationModelWithoutSolutionFromVRPREPModel(Paths.get(ClassLoader.class.getResource("/cvrp/christofides1979/" + workWith + ".xml").toURI()));

		JAXBContext context = JAXBContext.newInstance(CustomerTour.class);
		Unmarshaller um = context.createUnmarshaller();
		CustomerTour tour = (CustomerTour) um.unmarshal(new FileReader(ClassLoader.class.getResource("/cvrp/christofides1979/solutions/" + workWith + "_solution.xml").getFile()));

		SimpleBehaviorGenerator behaviourGenerator = new SimpleBehaviorGenerator();
		Behaviour behaviour = behaviourGenerator.createBehaviour(model, tour.getCustomerIds(), clock);

		model.setSolutionManager(new SolutionManager((network, structure) -> behaviour));

		init(mainProgramm, model, simulationEndTime);
		launch(args);
	}

}
