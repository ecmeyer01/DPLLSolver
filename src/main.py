import sys
import time
import json
from initialization import initialize
from dpll_step import dpll

# INITIALIZE

path = sys.argv[1]
original_membership, original_unassigned, original_clauses = initialize(path)

# RUN

start_time = time.time()
final_result, final_assigned = dpll(0, original_membership, original_unassigned, original_clauses, 0, 200)
end_time = time.time()
elapsed_time = end_time - start_time
final_assigned.discard(0)

# DISPLAY

final_assigned = sorted(list(final_assigned), key=abs)

if "input" in path:
    path = path[6:]

results_dict = {}
results_dict["Instance"] = path
results_dict["Time"] = round(elapsed_time, 2)

if final_result:
    results_dict["Result"] = "SAT"
    assigned_string = ""
    for val in final_assigned:
        assigned_string += str(abs(val))
        if val > 0:
            assigned_string += " true "
        else:
            assigned_string += " false "
    assigned_string = assigned_string[:-1]
    results_dict["Solution"] = assigned_string
else:
    results_dict["Result"] = "UNSAT"

results_json_string = json.dumps(results_dict)

print(results_json_string)