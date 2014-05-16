package recommendation.general.actionbased.graphbuilder;

import java.util.Collection;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import recommendation.general.actionbased.CollaborativeAction;

public interface ActionBasedGraphBuilder<Collaborator, Action extends CollaborativeAction<Collaborator>> {

	public String getName();
	
	public Graph<Collaborator, DefaultEdge> addActionToGraph(
			Graph<Collaborator, DefaultEdge> graph,
			Action currentAction,
			Collection<Action> pastActions);
}
