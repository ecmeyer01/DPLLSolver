import random
def select_next_branch_var(unassigned, clauses):
    i = random.randint(1, len(clauses)) - 1
    if clauses[i]:
        next_b = abs(next(iter(clauses[i])))
        unassigned.remove(next_b)
        return next_b
    else:
        return unassigned.pop()