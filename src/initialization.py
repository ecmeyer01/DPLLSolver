def initialize(path):
    original_clauses = []
    with open(path, "r") as file:
        if "input" not in path:
            next(file)
            next(file)  
        largest_variable = int(next(file).split()[2])
        num_vars = largest_variable + 1
        original_membership = {i: [] for i in range(-largest_variable, num_vars)}
        i = 0
        for line in file:
            chars = line.split()
            clause = set()
            should_include_clause = True
            for char in chars:
                val = int(char)
                if val != 0:
                    if (-val) in clause:
                        should_include_clause = False
                    clause.add(val)
            if clause and should_include_clause:
                for val in clause:
                    original_membership[val].append(i)
                original_clauses.append(clause)
                i += 1
    original_unassigned = set([i for i in range(1, num_vars)])
    return original_membership, original_unassigned, original_clauses