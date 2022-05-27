#!/usr/bin/python

import matplotlib.pyplot as plt
import numpy as np

mydpi = 300
pltsize = (6, 1.5)
transp = 0.8

# Milliseconds
data = {
'Integer |v| = 4': {
  'helib': 27702,
  'lattigo': 13782,
  'palisade': 4811,
  'palisade-1t': 21618,
  'seal': 25464,
  'tfhe': 104 },
'Binary |v| = 4': {
  'helib': 7388,
  'lattigo': 96341.97564100001,
  'palisade': 12219,
  'palisade-1t': 48772,
  'seal': 49446,
  'tfhe': 2018 },
'Integer |v| = 8': {
  'helib': 54002,
  'lattigo': 27395,
  'palisade': 9639,
  'palisade-1t': 43438,
  'seal': 50236,
  'tfhe': 104 },
'Binary |v| = 8': {
  'helib': 15052,
  'lattigo': 246567.374508,
  'palisade': 28175,
  'palisade-1t': 123013,
  'seal': 103,
  'tfhe': 3940 }
}

helib = []
lattigo = []
palisade = []
palisade_1t = []
seal = []
tfhe = []

x_axis_label = []
for k,val in data.items():
  x_axis_label.append(k)
  helib.append(val['helib'] / 1000)
  lattigo.append(val['lattigo'] / 1000)
  palisade.append(val['palisade'] / 1000)
  palisade_1t.append(val['palisade-1t'] / 1000)
  seal.append(val['seal'] / 1000)
  tfhe.append(val['tfhe'] / 1000)

N = len(palisade)
index = np.arange(N) # the x locations for the groups
width = 0.18 # the width of the bars

fig, ax = plt.subplots(figsize=pltsize)
ax.margins(0.02, 0.02)

rects1 = ax.bar(index - 3*width/2, helib, width,
                color='xkcd:light salmon', hatch='//', edgecolor='black', linewidth=1)
rects2 = ax.bar(index - width/2, lattigo, width,
                color='#ffddbf', hatch='xx', edgecolor='black', linewidth=1)
rects3_1t = ax.bar(index + width/2, palisade_1t, width,
                color='xkcd:ecru', edgecolor='black', linewidth=1, alpha=transp)
rects3 = ax.bar(index + width/2, palisade, width,
                color='xkcd:ecru', hatch='..', edgecolor='black', linewidth=1)
rects4 = ax.bar(index + 3*width/2, seal, width,
                color='xkcd:very light green', hatch='--', edgecolor='black', linewidth=1)
rects5 = ax.bar(index + 5*width/2, tfhe, width,
                color='xkcd:very light blue', hatch='\\\\', edgecolor='black', linewidth=1)

ax.set_axisbelow(True)
ax.grid(True, axis='y', which="major", linewidth = "0.3", linestyle='--')
ax.set_yscale('log')
ax.set_ylim([1, 8000])
ax.set_yticks([1, 10, 100, 1000])
ax.set_ylabel("Time (sec.)")
# ax.set_xlabel("Encrypted Domain")
ax.set_xticks(index + width / 2)
ax.set_xticklabels(x_axis_label)
ax.legend((rects1[0], rects2[0], rects3[0], rects3_1t[0], rects4[0], rects5[0]),
          ("HElib", "Lattigo", "PAL.", "PAL. 1C", "SEAL", "TFHE"),
          fontsize=8, ncol=6, loc='upper center')

def autolabel_above(rects, opacity = 1.0):
  for rect in rects:
    height = rect.get_height()
    if height*1000 == data['Binary |v| = 8']['seal']:
      ax.text(rect.get_x() + rect.get_width()/2., 5, 'Noisy', color='black', bbox=dict(facecolor='none', color='xkcd:very light green', linewidth=1, boxstyle='square'), ha='center', va='bottom', fontsize=8, rotation=90)
    elif height*1000 == data['Integer |v| = 4']['tfhe'] or height*1000 == data['Integer |v| = 8']['tfhe']:
      ax.text(rect.get_x() + rect.get_width()/2., 5, 'N/A', color='black', bbox=dict(facecolor='none', color='xkcd:very light blue', linewidth=1, boxstyle='square'), ha='center', va='bottom', fontsize=8, rotation=90)
    elif height > 10:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.0f' % (height), ha='center', va='bottom', fontsize=7, alpha=opacity)
    else:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.1f' % (height), ha='center', va='bottom', fontsize=7, alpha=opacity)

autolabel_above(rects1)
autolabel_above(rects2)
autolabel_above(rects3)
autolabel_above(rects3_1t, transp)
autolabel_above(rects4)
autolabel_above(rects5)

# plt.show()

plt.tight_layout()
plt.savefig("./hamming.png", dpi=mydpi, bbox_inches="tight", pad_inches=0.03)
