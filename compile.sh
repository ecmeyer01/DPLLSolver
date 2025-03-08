#!/bin/bash

#javac src/solver/sat/*.java

python -m py_compile src/initialization.py src/branch_execution_step.py src/unit_propagation_step.py src/pure_literal_elimination_step.py src/branch_selection_step.py src/dpll_step.py src/main.py
