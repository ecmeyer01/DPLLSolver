def remove_unit_vals(membership, assigned, unassigned, clauses):
    num_unit_vals_removed = 0
    unit_vals = set()
    while True:
        # Find unit values to remove
        for clause in clauses:
            if len(clause) == 1:
                u = next(iter(clause))
                if (-u) in unit_vals:
                    # Conflict found, indicating branch failure
                    return False, 0
                unit_vals.add(u)
        # If no unit values were found, end unit propagation step
        if not unit_vals:
            break
        # Remove the unit values
        while unit_vals:
            u = unit_vals.pop()
            unassigned.remove(abs(u))
            assigned.add(u)
            num_unit_vals_removed += 1
            for i in membership[u]:
                if u in clauses[i]:
                    clauses[i] = set()
            for i in membership[-u]:
                if (-u) in clauses[i]:
                    clauses[i].remove(-u)
    return True, num_unit_vals_removed