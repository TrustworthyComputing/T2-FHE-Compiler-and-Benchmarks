#!/usr/bin/python

import matplotlib.pyplot as plt
from matplotlib import colors
import numpy as np

mydpi = 300
pltsize = (6, 3)

# Milliseconds
data = {
'Integer'  : {
  'helib': 194,
  'lattigo': 134.87,
  'palisade': 43,
  'seal': 125,
  'tfhe': 44307 },
'Floating-Point' : {
  'helib': 341,
  'lattigo': 171.37,
  'palisade': 167,
  'seal': 101,
  'tfhe': 10 }
}

helib = []
lattigo = []
palisade = []
seal = []
tfhe = []

x_axis_label = []
for k,val in data.items():
  x_axis_label.append(k)
  helib.append(val['helib'] / 1000)
  lattigo.append(val['lattigo'] / 1000)
  palisade.append(val['palisade'] / 1000)
  seal.append(val['seal'] / 1000)
  tfhe.append(val['tfhe'] / 1000)

N = len(palisade)
index = np.arange(N) # the x locations for the groups
width = 0.16 # the width of the bars

fig, ax = plt.subplots(figsize=pltsize)
ax.margins(0.02, 0.02)

rects1 = ax.bar(index - 3*width/2 - 2*width/8, helib, width,
                color='xkcd:light salmon', hatch='//', edgecolor='black', linewidth=1)
rects2 = ax.bar(index - width/2 - width/8, lattigo, width,
                color='#ffddbf', hatch='xx', edgecolor='black', linewidth=1)
rects3 = ax.bar(index + width/2, palisade, width,
                color='xkcd:ecru', hatch='--', edgecolor='black', linewidth=1)
rects4 = ax.bar(index + 3*width/2 + width/8, seal, width,
                color='xkcd:very light green', hatch='..', edgecolor='black', linewidth=1)
rects5 = ax.bar(index + 5*width/2 + 2*width/8, tfhe, width,
                color='xkcd:very light blue', hatch='\\\\', edgecolor='black', linewidth=1)

ax.set_yscale('log')
ax.set_ylim([0.01, 100])
ax.set_ylabel("Time (sec.)")
ax.set_xlabel("Encrypted Domain")
ax.set_xticks(index + width / 2)
ax.set_xticklabels(x_axis_label)
ax.legend((rects1[0], rects2[0], rects3[0], rects4[0], rects5[0]),
          ("HElib", "Lattigo", "PALISADE", "SEAL", "TFHE"),
          fontsize=9, ncol=1, loc='upper left')

def autolabel_above(rects):
  for rect in rects:
    height = rect.get_height()
    if height <= 0.01:
      ax.text(rect.get_x() + rect.get_width()/2., 0.012, 'N/A', ha='center', va='bottom', fontsize=8)
      continue
    if height > 10:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.1f' % (height), ha='center', va='bottom', fontsize=8)
    else:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.2f' % (height), ha='center', va='bottom', fontsize=8)

autolabel_above(rects1)
autolabel_above(rects2)
autolabel_above(rects3)
autolabel_above(rects4)
autolabel_above(rects5)

# plt.show()

plt.tight_layout()
plt.savefig("./chi_squared.png", dpi=mydpi, bbox_inches="tight", pad_inches=0.03)
