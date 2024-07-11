import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from math import sqrt

def sort_keys(nums):
    return sorted(nums, key=int)

def sort_items(items):
    return sorted(items.items(), key=lambda x: int(x[0]))

def to_std(vals):
    return list(map(sqrt, vals))

def get_metrics(data, field):
    mean, var, q1, q3 = zip(*[x[field] for (_,x) in sort_items(data)])
    return mean, to_std(var), q1, q3

def get_count(data, field):
    return [x[field] for (_,x) in sort_items(data)]

def get_n_indexed_means(x, y, m):
    x = np.array(x)
    y = np.array(y)
    x_intervals = np.linspace(x.min(), x.max(), m + 1)

    # Find indices where x values fall within intervals
    indices = np.searchsorted(x, x_intervals)

    # Group y values based on indices
    grouped_y = [y[indices[i]:indices[i + 1]] for i in range(m)]

    # Calculate the mean of y for each range
    means_of_y = np.array([np.mean(group) for group in grouped_y])

    return indices, means_of_y

def plot(data, title):
    completed = get_count(data, 'completed')
    arrived = get_count(data, 'arrived')
    tisam, tisav, tisaq1, tisaq3 = get_metrics(data, 'tisa')
    tiscm, tiscv, tiscq1, tiscq3 = get_metrics(data, 'tisc')
    tiqam, tiqav, tiqaq1, tiqaq3  = get_metrics(data, 'tiqa')
    tiqcm, tiqcv, tiqcq1, tiqcq3 = get_metrics(data, 'tiqc')

    x = np.array([int(x)/100 for x in data.keys()])

    fig, ax1 = plt.subplots()
    ax1.set_ylabel('Time (s)')
    ax1.set_xlabel('$\lambda$')


    #plt.plot(x, tisam, label='Time In System (All)', color='blue')
    tisplot = ax1.plot(x, tiscm, label='Avg Time In System', color='green')
    #plt.plot(x, tiqam, label='Time In Queue (All)', color='red')
    tiqplot = ax1.plot(x, tiqcm, label='Avg Time In Queue', color='red')
    #plt.fill_between(x, [a-b for a,b in zip(tisam, tisav)], [a+b for a,b in zip(tisam, tisav)], color='blue', alpha=0.2)
    #plt.fill_between(x, [a-b for a,b in zip(tiscm, tiscv)], [a+b for a,b in zip(tiscm, tiscv)], color='green', alpha=0.2)
    #plt.fill_between(x, [a-b for a,b in zip(tiqam, tiqav)], [a+b for a,b in zip(tiqam, tiqav)], color='red', alpha=0.2)
    #plt.fill_between(x, [a-b for a,b in zip(tiqcm, tiqcv)], [a+b for a,b in zip(tiqcm, tiqcv)], color='red', alpha=0.2)
    ax1.fill_between(x, tiscq1, tiscq3, color='green', alpha=0.2)
    ax1.fill_between(x, tiqcq1, tiqcq3, color='red', alpha=0.2)

    ax2 = ax1.twinx()
    n_means = 10
    intervals, mean_completeds = get_n_indexed_means(x, completed, n_means)
    #heights = (mean_completeds / mean_completeds.max()) * max(np.array(tiscm).max(), np.array(tiqcm).max())
    ax2.set_ylabel('Number of completed tasks')
    cbplot = ax2.bar(x[intervals[:-1]], mean_completeds, label='Completed', color='blue', alpha=0.2, width=0.5/(n_means+1), align='edge')


    fig.suptitle(title)
    #fig.legend(loc='upper left')
    #fig.legend(handles = [tisplot[0], tiqplot[0], cbplot], loc='upper left')
    fig.legend(bbox_to_anchor=(0.1, 0.85), loc='upper left')
    plt.tight_layout()
    plt.show()

def main():
    with open('fixed.json') as f:
        fixed = eval(f.read())

    with open('exponential.json') as f:
        exponential = eval(f.read())

    plot(fixed, 'Average times in system and queue for $\mu = 1$')
    plot(exponential, 'Average times in system and queue for $\mu \sim Exp(1)$')

if __name__ == '__main__':
    main()

