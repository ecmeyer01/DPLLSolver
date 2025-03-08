from branch_execution_step import remove_branch_var
from unit_propagation_step import remove_unit_vals
from pure_literal_elimination_step import remove_pure_vals
from branch_selection_step import select_next_branch_var

def dpll(b, membership, unassigned, clauses, num_vals_removed_since_last_ple, ple_freq):
    assigned = set()
    remove_branch_var(b, membership, assigned, clauses)
    num_vals_removed_since_last_ple += 1
    valid, num_unit_vals_removed = remove_unit_vals(membership, assigned, unassigned, clauses)
    if not valid:
        return False, set()
    num_vals_removed_since_last_ple += num_unit_vals_removed
    if num_vals_removed_since_last_ple > ple_freq:
        remove_pure_vals(membership, assigned, unassigned, clauses)
        num_vals_removed_since_last_ple = 0
    if unassigned:
        next_b = select_next_branch_var(unassigned, clauses)
        result, new_assigned = dpll(next_b, membership, unassigned.copy(), [clause.copy() for clause in clauses], num_vals_removed_since_last_ple, ple_freq) # copy needed
        if result:
            assigned.update(new_assigned)
            return True, assigned
        else:
            result, new_assigned = dpll(-next_b, membership, unassigned, clauses, num_vals_removed_since_last_ple, ple_freq) # no copy needed
            if result:
                assigned.update(new_assigned)
                return True, assigned
            else:
                return False, set()
    else:
        return True, assigned