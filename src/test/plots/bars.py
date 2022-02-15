#!/usr/bin/python

import matplotlib.pyplot as plt
from matplotlib import colors
import numpy as np
import math

mydpi = 300
pltsize = (6, 3)

data = {
'bencha'  : {
  'helib': 2,
  'lattigo': 5,
  'palisade': 4.2,
  'seal': 100,
  'tfhe': 110 },
'benchb' : {
  'helib': 28,
  'lattigo': 15,
  'palisade': 30.1,
  'seal': 100,
  'tfhe': 110 },
'benchc' : {
  'helib': 65,
  'lattigo': 8.7,
  'palisade': 56.4,
  'seal': 100,
  'tfhe': 110 },
'benchd': {
  'helib': 112,
  'lattigo': 14.2,
  'palisade': 95.3,
  'seal': 100 ,
  'tfhe': 110}
}

helib = []
lattigo = []
palisade = []
seal = []
tfhe = []

x_axis_label = []
for k,val in data.items():
  x_axis_label.append(k)
  helib.append(val['helib'])
  lattigo.append(val['lattigo'])
  palisade.append(val['palisade'])
  seal.append(val['seal'])
  tfhe.append(val['tfhe'])

N = len(palisade)
index = np.arange(N) # the x locations for the groups
width = 0.16 # the width of the bars

fig, ax = plt.subplots(figsize=pltsize)
ax.margins(0.02, 0.02)

rects1 = ax.bar(index - 3*width/2 - 2*width/6, palisade, width,
                color='xkcd:light salmon', hatch='//', edgecolor='black', linewidth=1)
rects2 = ax.bar(index - width/2 - width/6, palisade, width,
                color='#ffddbf', hatch='xx', edgecolor='black', linewidth=1)
rects3 = ax.bar(index + width/2, lattigo, width,
                color='xkcd:ecru', hatch='--', edgecolor='black', linewidth=1)
rects4 = ax.bar(index + 3*width/2 + width/6, seal, width,
                color='xkcd:very light green', hatch='..', edgecolor='black', linewidth=1)
rects5 = ax.bar(index + 5*width/2 + 2*width/6, seal, width,
                color='xkcd:very light blue', hatch='\\\\', edgecolor='black', linewidth=1)

ax.set_yscale('log')
ax.set_ylim([1, 1000])
ax.set_ylabel("Time (sec.)")
ax.set_xlabel("Selected ... benchmarks")
ax.set_xticks(index + width / 2)
ax.set_xticklabels(x_axis_label)
ax.legend((rects1[0], rects2[0], rects3[0], rects4[0], rects5[0]),
          ("HElib", "Lattigo", "PALISADE", "SEAL", "TFHE"),
          fontsize=9, ncol=3, loc='upper left')

def autolabel_above(rects):
    for rect in rects:
        height = rect.get_height()
        if height > 10:
            ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.0f' % (height), ha='center', va='bottom', fontsize=8)
        else:
            ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.1f' % (height), ha='center', va='bottom', fontsize=8)

autolabel_above(rects1)
autolabel_above(rects2)
autolabel_above(rects3)
autolabel_above(rects4)
autolabel_above(rects5)

# plt.show()

plt.tight_layout()
plt.savefig("./bars.png", dpi=mydpi, bbox_inches="tight", pad_inches=0.03)
