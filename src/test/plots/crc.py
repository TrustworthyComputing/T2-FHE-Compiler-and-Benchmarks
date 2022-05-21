#!/usr/bin/python

import matplotlib.pyplot as plt
from matplotlib import colors
import numpy as np

mydpi = 300
pltsize = (6, 1.5)
transp = 0.5

# Milliseconds
data = {
'CRC-8'  : {
  'helib': 12053,
  'lattigo': 58735.073422,
  'lattigo-1t': 60200,
  'palisade': 36400,
  'palisade-1t': 124935,
  'seal': 109441,
  'tfhe': 4018 },
'CRC-32' : {
  'helib': 2433054,
  'lattigo': 101,
  'lattigo-1t': 101,
  'palisade': 102,
  'palisade-1t': 102,
  'seal': 103,
  'tfhe': 66637 },
}

helib = []
lattigo = []
lattigo_1t = []
palisade = []
palisade_1t = []
seal = []
tfhe = []

x_axis_label = []
for k,val in data.items():
  x_axis_label.append(k)
  helib.append(val['helib'] / 1000)
  lattigo.append(val['lattigo'] / 1000)
  lattigo_1t.append(val['lattigo-1t'] / 1000)
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
rects2_1t = ax.bar(index - width/2, lattigo_1t, width,
                color='#ffddbf', edgecolor='black', linewidth=1, alpha=transp)
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
ax.set_ylim([1, 10000])
ax.set_yticks([1, 10, 100, 1000, 10000])
ax.set_ylabel("Time (sec.)")
# ax.set_xlabel("Binary Domain")
ax.set_xticks(index + width / 2)
ax.set_xticklabels(x_axis_label)
ax.legend((rects1[0], rects2[0], rects3[0], rects4[0], rects5[0]),
          ("HElib", "Lattigo", "PALIS.", "SEAL", "TFHE"),
          fontsize=8, ncol=1, loc='center right', bbox_to_anchor=(1.21, 0.5))

def autolabel_above(rects, opacity = 1.0):
  for rect in rects:
    height = rect.get_height()
    if height <= 3.0:
      if height*1000 == data['CRC-32']['lattigo']:
        ax.text(rect.get_x() + rect.get_width()/2., 5, 'Noisy', color='black', bbox=dict(facecolor='none', color='#ffddbf', linewidth=5, boxstyle='square'), ha='center', va='bottom', fontsize=8, rotation=90)
      elif height*1000 == data['CRC-32']['palisade']:
        ax.text(rect.get_x() + rect.get_width()/2., 5, 'Noisy', color='black', bbox=dict(facecolor='none', color='xkcd:ecru', linewidth=5, boxstyle='square'), ha='center', va='bottom', fontsize=8, rotation=90)
      elif height*1000 == data['CRC-32']['seal']:
        ax.text(rect.get_x() + rect.get_width()/2., 5, 'Noisy', color='black', bbox=dict(facecolor='none', color='xkcd:very light green', linewidth=5, boxstyle='square'), ha='center', va='bottom', fontsize=8, rotation=90)
      continue
    if height < 100:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.1f' % (height), ha='center', va='bottom', fontsize=7, alpha=opacity)
    else:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.0f' % (height), ha='center', va='bottom', fontsize=7, alpha=opacity)

autolabel_above(rects1)
autolabel_above(rects2)
autolabel_above(rects3)
autolabel_above(rects3_1t, transp)
autolabel_above(rects4)
autolabel_above(rects5)

# plt.show()

plt.tight_layout()
plt.savefig("./crc.png", dpi=mydpi, bbox_inches="tight", pad_inches=0.03)
