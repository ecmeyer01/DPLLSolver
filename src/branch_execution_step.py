def remove_branch_var(b, membership, assigned, clauses):
    assigned.add(b)
    for i in membership[b]:
        if b in clauses[i]:
            clauses[i] = set()
    for i in membership[-b]:
        if (-b) in clauses[i]:
            clauses[i].remove(-b)
    return