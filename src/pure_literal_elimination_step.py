def remove_pure_vals(membership, assigned, unassigned, clauses):
    # Find pure values to remove
    non_pure_vals = set()
    traversed_vals = set()
    for clause in clauses:
        for val in clause:
            if (-val) in traversed_vals:
                non_pure_vals.add(val)
                non_pure_vals.add(-val)
            traversed_vals.add(val)
    pure_vals = traversed_vals - non_pure_vals
    # Remove the pure values
    while pure_vals:
        p = pure_vals.pop()
        unassigned.remove(abs(p))
        assigned.add(p)
        for i in membership[p]:
            if p in clauses[i]:
                clauses[i] = set()
    # (Multiple iterations might hurt performance)
    return