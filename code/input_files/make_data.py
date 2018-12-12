from sys import argv
from sklearn.utils.random import sample_without_replacement
import numpy as np

def n_random_points(dims, nsamp):
    idx = sample_without_replacement(np.prod(dims), nsamp)
    return np.vstack(np.unravel_index(idx, dims)).T


def n_points_on_circle(n):
    return [(np.cos(2 * np.pi / n * x) * n, np.sin(2 * np.pi / n * x) * n) for x in range(0, n + 1)]


if len(argv) < 3:
    print('Usage: make_data <num points> <file name>')
    exit()

n = int(argv[1])
dest = argv[2]
limits = (100, 200)

with open(dest, "w+") as f:
    #random_points = n_random_points(limits, n)
    random_points = n_points_on_circle(n)
    for point in random_points:
        f.write(f"{point[0]},{point[1]}\n")


