import csv
import sys

import networkx as nx


def read_nfa_csv(file):
    with open(file, newline='\n') as f:
        reader = csv.reader(f, delimiter=';')
        graph = nx.MultiDiGraph()
        is_final = reader.__next__()[1:]
        states = reader.__next__()[1:]
        for state, is_final in zip(states, is_final):
            graph.add_node(state, is_final=(is_final == 'F'))
        for line in reader:
            signal = line[0]
            to_states = line[1:]
            for from_state, to_state_set in zip(states, to_states):
                if to_state_set != '-' and to_state_set != '':
                    for to_state in to_state_set.split(','):
                        graph.add_edge(from_state, to_state.strip(), signal=signal)
        return graph


def write_dfa_csv(graph: nx.DiGraph, file: str):
    with open(file, 'w', newline='\n') as f:
        writer = csv.writer(f, delimiter=';')
        ordered_states = sorted(list(graph.nodes))
        indexed_states = dict(zip(ordered_states, range(len(ordered_states))))
        ordered_state_outs = ['F' if graph.nodes[node]['is_final'] else '' for node in ordered_states]
        ordered_signals = sorted(list(set(nx.get_edge_attributes(graph, "signal").values())))
        indexed_signals = dict(zip(ordered_signals, range(len(ordered_signals))))
        writer.writerow([''] + ordered_state_outs)
        writer.writerow([''] + ordered_states)
        transitions_matrix = [[signal] + ["-"] * len(ordered_states) for signal in ordered_signals]

        for from_state, to_state in graph.edges:
            data = graph.get_edge_data(from_state, to_state)
            signal = data["signal"]
            transitions_matrix[indexed_signals[signal]] \
                [indexed_states[from_state] + 1] = to_state
        writer.writerows(transitions_matrix)


def first(iterable):
    for el in iterable:
        return el


def get_epsilon_closure(graph: nx.DiGraph, state):
    closure = set()
    closure.add(state)
    closure_stack = [state]

    while (len(closure_stack) > 0):
        cur = closure_stack.pop(0)
        for edge in graph.out_edges(cur, data=True):
            if edge[2]['signal'] == 'e':
                x = edge[1]
                if x not in closure:
                    closure.add(x)
                    closure_stack.append(x)
    return closure


def determine_machine(graph: nx.MultiDiGraph):
    state_naming_map = dict()
    state_naming_i = [0]
    result = nx.DiGraph()

    epsilon_closure_cache = dict(
        [
            (state, get_epsilon_closure(graph, state))
            for state in graph.nodes
        ]
    )

    def get_state_name(state_list):
        state_list = sorted(state_list)
        naming = ','.join(state_list)
        if naming not in state_naming_map:
            state_naming_map[naming] = 'X' + str(state_naming_i[0])
            state_naming_i[0] += 1
        return state_naming_map[naming]

    initial_states = list(epsilon_closure_cache[first(graph.nodes)])
    state_queue = [initial_states]

    result.add_node(get_state_name(initial_states), is_final=any(graph.nodes[out]['is_final'] for out in initial_states))
    while len(state_queue) > 0:
        state = state_queue.pop(0)

        state_name = get_state_name(state)

        sum_outs = dict()
        for substate in state:
            for out_edge in graph.out_edges(substate, data=True):
                signal = out_edge[2]['signal']

                chain = sum_outs.get(signal, set())
                chain.update(epsilon_closure_cache[out_edge[1]])
                sum_outs[signal] = chain

        if 'e' in sum_outs:
            del sum_outs['e']

        for out in sum_outs.keys():
            states = sum_outs[out]
            new_name = get_state_name(states)
            if new_name not in result.nodes:
                result.add_node(new_name, is_final=any(graph.nodes[out]['is_final'] for out in states))
                state_queue.append(states)
            result.add_edge(state_name, new_name, signal=out)

    return result, state_naming_map


def exit_help():
    print('nfa_dfa.py INPUT_FILENAME OUTPUT_FILENAME')
    sys.exit(0)


if __name__ == '__main__':
    args = sys.argv[1:]

    if len(args) != 2:
        exit_help()

    nfa = read_nfa_csv(args[0])
    dfa, naming = determine_machine(nfa)
    for states, to_state in naming.items():
        print(to_state + " -> " + str(states))
    write_dfa_csv(dfa, args[1])

    print('OK ' + str(len(dfa.nodes)) + " states")
