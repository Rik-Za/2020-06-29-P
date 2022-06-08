package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	private Graph<Match, DefaultWeightedEdge> grafo;
	private PremierLeagueDAO dao;
	private Map<Integer,Match> vertici;
	private List<Coppia> archi;
	private List<Match> migliore;
	private double pesoOttimo;
	private Set<Match> visitabili;
	
	public Model() {
		this.dao=new PremierLeagueDAO();
		this.vertici= new HashMap<>();
		this.archi= new ArrayList<>();
		
	}
	
	public String creaGrafo(int minuti, int mese) {
		this.grafo= new SimpleWeightedGraph<Match, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		//aggiungo vertici
		this.vertici=this.dao.getVertici(mese);
		Graphs.addAllVertices(this.grafo, this.vertici.values());
		//aggiungo archi
		this.archi= this.dao.getArchi(mese, minuti, vertici);
		for(Coppia c: archi)
			Graphs.addEdge(this.grafo, c.getM1(), c.getM2(), c.getPeso());
		String s="GRAFO CREATO!\n";
		s+="#VERTICI: "+this.grafo.vertexSet().size()+"\n";
		s+="#ARCHI: "+this.grafo.edgeSet().size()+"\n";
		return s;
		
	}
	
	public List<Coppia> connessioniMax(){
		//Map<Integer,Team> team = new HashMap<>();
		//team = this.dao.getTeam();
		List<Coppia> ris = new ArrayList<Coppia>();
		int max=0;
		for(Coppia c: this.archi) {
			if(c.getPeso()>max)
				max=(int) c.getPeso();
		}
		for(Coppia c: this.archi)
			if(c.getPeso()==max) {
				//c.getM1().setTeamHomeNAME(team.get(c.getM1().getTeamHomeID()).getName());
				//c.getM2().setTeamAwayNAME(team.get(c.getM2().getTeamAwayID()).getName());
				ris.add(c);
			}
				
		return ris;
	}
	
	public List<Match> getVertici(){
		List<Match> ris = new ArrayList<>(this.vertici.values());
		return ris;
	}
	
	public int getPesoOttimo() {
		return (int) this.pesoOttimo;
	}
	
	public List<Match> calcolaPercorso(Match m1, Match m2){
		migliore = new ArrayList<Match>();
		pesoOttimo=0;
		List<Match> parziale = new ArrayList<Match>();
		parziale.add(m1);
		ConnectivityInspector<Match, DefaultWeightedEdge> it = new ConnectivityInspector<>(grafo);
		visitabili = it.connectedSetOf(m1);
		if(!visitabili.contains(m2))
			return null;
		cerca(parziale, 0, m2);
		return migliore;
	}

	private void cerca(List<Match> parziale, int peso, Match m2) {
		// condizione terminazione
		if(parziale.get(parziale.size()-1).equals(m2)) {
			if(peso>pesoOttimo)
			{
				pesoOttimo=peso;
				migliore = new ArrayList<Match>(parziale);
			}
			return;
		}
		Match ultimo=parziale.get(parziale.size()-1);
		for(Match m: Graphs.neighborListOf(this.grafo,ultimo)) {
			//boolean b = (m.getTeamHomeID()==ultimo.getTeamHomeID() && m.getTeamAwayID()==ultimo.getTeamAwayID()) || (m.getTeamHomeID()==ultimo.getTeamAwayID() && m.getTeamAwayID() == ultimo.getTeamHomeID());
			if((m.getTeamHomeID()==ultimo.getTeamHomeID() && m.getTeamAwayID()==ultimo.getTeamAwayID()) || (m.getTeamHomeID()==ultimo.getTeamAwayID() && m.getTeamAwayID() == ultimo.getTeamHomeID()))
				continue;
			else
			//if(!b)
			{	
			if(!parziale.contains(m)) {
				parziale.add(m);
				int pesoArco= (int) this.grafo.getEdgeWeight(this.grafo.getEdge(ultimo, m));
				cerca(parziale, peso+pesoArco,m2);
				parziale.remove(parziale.size()-1);
			}
			}
		}
		
	}
	
}
