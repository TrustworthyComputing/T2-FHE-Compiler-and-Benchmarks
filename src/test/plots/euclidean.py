#!/usr/bin/python

import matplotlib.pyplot as plt
import numpy as np

mydpi = 300
pltsize = (6, 1.5)
transp = 0.8

# Milliseconds
data = {
'Int. $|v| = 64$' : {
  'helib': 872,
  'lattigo': 257.121658,
  'palisade': 181,
  'palisade-1t': 333,
  'seal': 313,
  'tfhe': 328060 },
'FP $|v| = 64$' : {
  'helib': 1620,
  'lattigo': 153.466697,
  'palisade': 318,
  'palisade-1t': 379,
  'seal': 289,
  'tfhe': 14 },
'Int. $|v| = 128$' : {
  'helib': 1766,
  'lattigo': 519.89226,
  'palisade': 415,
  'palisade-1t': 629,
  'seal': 625,
  'tfhe': 647391 },
'FP $|v| = 128$' : {
  'helib': 3338,
  'lattigo': 308.13639,
  'palisade': 743,
  'palisade-1t': 899,
  'seal': 584,
  'tfhe': 14 },
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
ax.grid(True, axis='y', which="both", linewidth = "0.3", linestyle='--')
ax.set_yscale('log')
ax.set_ylim([0.1, 9000])
ax.set_yticks([0.1, 1, 10, 100, 1000])
ax.set_ylabel("Time (sec.)")
# ax.set_xlabel("Encrypted Domain")
ax.set_xticks(index + width / 2)
ax.set_xticklabels(x_axis_label)
ax.legend((rects1[0], rects2[0], rects3[0], rects3_1t[0], rects4[0], rects5[0]),
          ("HElib", "Lattigo", "PALIS.", "PAL. 1C", "SEAL", "TFHE"),
          fontsize=8, ncol=2, loc='upper center')

def autolabel_above(rects):
  for rect in rects:
    height = rect.get_height()
    if height*1000 == data['FP $|v| = 64$']['tfhe'] or height*1000 == data['FP $|v| = 128$']['tfhe']:
      ax.text(rect.get_x() + rect.get_width()/2., 0.4, 'N/A', color='black', bbox=dict(facecolor='none', color='xkcd:very light blue', linewidth=1, boxstyle='square'), ha='center', va='bottom', fontsize=8, rotation=90)
    elif height < 0.25:
      ax.text(rect.get_x() + rect.get_width()/2., 1.9*height, '%2.1f' % (height), ha='center', va='bottom', fontsize=7)
    elif height < 100:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.1f' % (height), ha='center', va='bottom', fontsize=7)
    else:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.0f' % (height), ha='center', va='bottom', fontsize=7)

autolabel_above(rects1)
autolabel_above(rects2)
autolabel_above(rects3)
autolabel_above(rects4)
autolabel_above(rects5)

# plt.show()

plt.tight_layout()
plt.savefig("./euclidean.png", dpi=mydpi, bbox_inches="tight", pad_inches=0.03)
