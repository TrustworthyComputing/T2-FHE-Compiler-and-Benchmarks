import numpy as np
import matplotlib.pyplot as plt
import sys

mydpi = 300

pltsize = (6, 3)

data = {
'4x4'  : {
  'helib': 2,
  'lattigo': 5,
  'palisade': 10,
  'seal': 15,
  'tfhe': 20 },
'8x8' : {
  'helib': 5,
  'lattigo': 7,
  'palisade': 12,
  'seal': 17,
  'tfhe': 22 },
'16x16' : {
  'helib': 7,
  'lattigo': 9,
  'palisade': 14,
  'seal': 19,
  'tfhe': 24 },
}

helib = []
lattigo = []
palisade = []
seal = []
tfhe = []

x_axis_label = []
for k, val in data.items():
  x_axis_label.append(k)
  helib.append(val['helib'])
  lattigo.append(val['lattigo'])
  palisade.append(val['palisade'])
  seal.append(val['seal'])
  tfhe.append(val['tfhe'])


N = len(x_axis_label)
index = np.arange(N)  # the x locations for the groups

fig, ax = plt.subplots(figsize=pltsize)
ax.set_xticks(index)
ax.set_xlabel('Benchmarks ....')
ax.set_xticklabels(x_axis_label)

l1 = ax.plot(index, helib, linestyle='solid', color='black',
             markerfacecolor='salmon', marker='o', linewidth=1, markersize=8)
l2 = ax.plot(index, lattigo, linestyle='solid', color='black',
             markerfacecolor='xkcd:light orange', marker='D', linewidth=1, markersize=8)
l3 = ax.plot(index, palisade, linestyle='solid', color='black',
             markerfacecolor='xkcd:butter yellow', marker='^', linewidth=1, markersize=8)
l4 = ax.plot(index, seal, linestyle='solid', color='black',
             markerfacecolor='xkcd:green', marker='s', linewidth=1, markersize=8)
l5 = ax.plot(index, tfhe, linestyle='solid', color='black',
             markerfacecolor='xkcd:sky blue', marker='v', linewidth=1, markersize=8)

ax.set_ylim([1, 100])
ax.set_yscale('log')
ax.set_ylabel('Time (sec.)')


ax.legend((l1[0], l2[0], l3[0], l4[0], l5[0]),
          ("HElib", "Lattigo", "PALISADE", "SEAL", "TFHE"),
          fontsize=9, ncol=3, loc='upper left')


plt.tight_layout()
plt.savefig('./linechart.png', dpi=mydpi, bbox_inches='tight', pad_inches=0.03)
