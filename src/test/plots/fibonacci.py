#!/usr/bin/python

import matplotlib.pyplot as plt
from matplotlib import colors
import numpy as np

mydpi = 300
pltsize = (6, 2.3)

# Milliseconds
data = {
'20'  : {
  'helib': 1,
  'lattigo': 1.744696,
  'palisade': 337,
  'seal': 1,
  'tfhe': 13502 },
'30' : {
  'helib': 1,
  'lattigo': 2.423624,
  'palisade': 498,
  'seal': 1,
  'tfhe': 20343 },
'40' : {
  'helib': 1,
  'lattigo': 2.685706,
  'palisade': 683,
  'seal': 1,
  'tfhe': 26964 }
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
ax.set_ylim([0.1, 10000])
ax.set_ylabel("Time (sec.)")
ax.set_xlabel("Matrices Size")
ax.set_xticks(index + width / 2)
ax.set_xticklabels(x_axis_label)
ax.legend((rects1[0], rects2[0], rects3[0], rects4[0], rects5[0]),
          ("HElib", "Lattigo", "PALISADE", "SEAL", "TFHE"),
          fontsize=9, ncol=2, loc='upper left')

def autolabel_above(rects):
  for rect in rects:
    height = rect.get_height()
    if height < 0.1:
      ax.text(rect.get_x() + rect.get_width()/2., 0.11, '%2.1f' % (height), ha='center', va='bottom', fontsize=8)
    elif height > 10:
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
plt.savefig("./fibonacci.png", dpi=mydpi, bbox_inches="tight", pad_inches=0.03)
