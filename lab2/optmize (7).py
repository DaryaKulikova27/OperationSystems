import csv
import sys

import networkx as nx

def read_mealy_csv(file):
    with open(file, newline='\n') as f:
        reader = csv.reader(f, delimiter=';')
        graph = nx.DiGraph()
        states = reader.__next__()[1:]  # ['a1', 'a2', 'a3']
        for line in reader:
            signal = line[0]  # z1
            transitions = line[1:]  # ['a3/w1', 'a1/w1', 'a1/w2']
            for transition, from_state in zip(transitions, states):
                to_state, out_signal = transition.split('/')  # a3, w1
                graph.add_edge(from_state, to_state, signal=signal, out_signal=out_signal)
        return graph


def read_moore_csv(file):
    with open(file, newline='\n') as f:
        reader = csv.reader(f, delimiter=';')
        graph = nx.DiGraph()
        out_signals = reader.__next__()[1:]
        states = reader.__next__()[1:]
        for state, out_signal in zip(states, out_signals):
            graph.add_node(state, out_signal=out_signal)
        for line in reader:
            signal = line[0]
            to_states = line[1:]
            for from_state, to_state in zip(states, to_states):
                graph.add_edge(from_state, to_state, signal=signal)
        return graph


def write_mealy_csv(graph: nx.DiGraph, file: str):
    with open(file, 'w', newline='\n') as f:
        writer = csv.writer(f, delimiter=';')
        ordered_states = sorted(list(graph.nodes))
        indexed_states = dict(zip(ordered_states, range(len(ordered_states))))
        ordered_signals = sorted(list(set(nx.get_edge_attributes(graph, "signal").values())))
        indexed_signals = dict(zip(ordered_signals, range(len(ordered_signals))))
        writer.writerow([''] + ordered_states)
        transitions_matrix = [[signal] + [""] * len(ordered_states) for signal in ordered_signals]

        for from_state, to_state in graph.edges:
            data = graph.get_edge_data(from_state, to_state)
            signal, out_signal = data["signal"], data["out_signal"]
            transitions_matrix[indexed_signals[signal]] \
                [indexed_states[from_state] + 1] = to_state + '/' + out_signal
        writer.writerows(transitions_matrix)


def write_moore_csv(graph: nx.DiGraph, file: str):
    with open(file, 'w', newline='\n') as f:
        writer = csv.writer(f, delimiter=';')
        ordered_states = sorted(list(graph.nodes))
        indexed_states = dict(zip(ordered_states, range(len(ordered_states))))
        ordered_state_outs = [graph.nodes[node]['out_signal'] for node in ordered_states]
        ordered_signals = sorted(list(set(nx.get_edge_attributes(graph, "signal").values())))
        indexed_signals = dict(zip(ordered_signals, range(len(ordered_signals))))
        writer.writerow([''] + ordered_state_outs)
        writer.writerow([''] + ordered_states)
        transitions_matrix = [[signal] + [""] * len(ordered_states) for signal in ordered_signals]

        for from_state, to_state in graph.edges:
            data = graph.get_edge_data(from_state, to_state)
            signal = data["signal"]
            transitions_matrix[indexed_signals[signal]] \
                [indexed_states[from_state] + 1] = to_state
        writer.writerows(transitions_matrix)


def cluster_by(graph: nx.DiGraph, key):
    def get_signals(tr):
        return tr[2][key], tr[2]['signal']

    def get_first(tr):
        return tr[0]

    clusters = dict()
    clusters_names = dict()

    synthetic_i = 1

    for node in graph.nodes:
        node_data = graph.nodes[node]
        group_attachment = [node_data['group']] if 'group' in node_data else []
        node_transitions = tuple(sorted(
            map(get_signals, graph.out_edges(node, data=True)),
            key=get_first
        ) + group_attachment)

        chain = clusters.get(node_transitions, [])
        chain.append(node)
        clusters[node_transitions] = chain

        group_name = clusters_names.get(node_transitions)
        if group_name is None:
            group_name = 'X' + str(synthetic_i)
            synthetic_i += 1
        clusters_names[node_transitions] = group_name

        node_data['group'] = group_name

    for transition in graph.edges:
        graph.edges[transition[0], transition[1]]['group_out'] = graph.nodes[transition[1]]['group']

    return list(clusters_names.values()), clusters.values()


def cluster_by_nodes(graph: nx.DiGraph, key):
    clusters = dict()
    clusters_names = dict()

    synthetic_i = 1

    for node in graph.nodes(data=True):
        search_key = node[1][key]

        chain = clusters.get(search_key, [])
        chain.append(node[0])
        clusters[search_key] = chain

        group_name = clusters_names.get(search_key)
        if group_name is None:
            group_name = 'X' + str(synthetic_i)
            synthetic_i += 1
        clusters_names[search_key] = group_name

        graph.nodes[node[0]]['group'] = group_name

    for transition in graph.edges:
        graph.edges[transition[0], transition[1]]['group_out'] = graph.nodes[transition[1]]['group']

    return list(clusters_names.values()), clusters.values()


def optimize_mealy(graph: nx.DiGraph) -> nx.DiGraph:
    clusters, last_cluster_groups = cluster_by(graph, 'out_signal')
    prev_clusters = []
    while len(clusters) != len(prev_clusters):
        prev_clusters = clusters
        clusters, last_cluster_groups = cluster_by(graph, 'group_out')

    minimized = nx.DiGraph()

    for group in last_cluster_groups:
        selected_node = group[0]
        name = graph.nodes[selected_node]['group']

        for edge in graph.out_edges(selected_node, data=True):
            minimized.add_edge(name, graph.nodes[edge[1]]['group'], **edge[2])

    return minimized


def optimize_moore(graph: nx.DiGraph) -> nx.DiGraph:
    clusters, last_cluster_groups = cluster_by_nodes(graph, 'out_signal')
    prev_clusters = []
    while len(clusters) != len(prev_clusters) or len(clusters) == 0:
        prev_clusters = clusters
        clusters, last_cluster_groups = cluster_by(graph, 'group_out')

    minimized = nx.DiGraph()

    for group in last_cluster_groups:
        selected_node = group[0]
        name = graph.nodes[selected_node]['group']
        minimized.add_node(name, **graph.nodes[selected_node])

        for edge in graph.out_edges(selected_node, data=True):
            minimized.add_edge(name, graph.nodes[edge[1]]['group'], **edge[2])

    return minimized


def exit_help():
    print('convert.py (mealy|moore) INPUT_FILENAME OUTPUT_FILENAME')
    sys.exit(0)


if __name__ == '__main__':
    args = sys.argv[1:]

    if len(args) != 3:
        exit_help()

    if args[0] == 'mealy':
        mealy = read_mealy_csv(args[1])
        result = optimize_mealy(mealy)
        write_mealy_csv(result, args[2])
        print('OK')
    elif args[0] == 'moore':
        moore = read_moore_csv(args[1])
        result = optimize_moore(moore)
        write_moore_csv(result, args[2])
        print('OK')
    else:
        exit_help()
