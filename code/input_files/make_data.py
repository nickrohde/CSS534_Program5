from sys import argv
from sklearn.utils.random import sample_without_replacement
import numpy as np

def n_random_points(dims, nsamp):
    idx = sample_without_replacement(np.prod(dims), nsamp)
    return np.vstack(np.unravel_index(idx, dims)).T

if len(argv) < 3:
	print('Usage: make_data <num points> <file name>')
	exit()

n = int(argv[1])
dest = argv[2]
limits = (100, 200)

with open(dest, "w+") as f:
	random_points = n_random_points(limits, n)
	for point in random_points:
		f.write(f"{point[0]},{point[1]}\n")


