#!/usr/bin/python

import matplotlib.pyplot as plt
import numpy as np

mydpi = 300
pltsize = (6, 1.5)

# Milliseconds
data = {
'$|v| = 4$': {
  'helib': 62765,
  'lattigo': 101,
  'palisade': 102,
  'seal': 103,
  'tfhe': 4000 },
'$|v| = 8$': {
  'helib': 2308615,
  'lattigo': 101,
  'palisade': 102,
  'seal': 103,
  'tfhe': 18662 },
'$|v| = 16$': {
  'helib': 11457458,
  'lattigo': 101,
  'palisade': 102,
  'seal': 103,
  'tfhe': 76287 }
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
width = 0.18 # the width of the bars

fig, ax = plt.subplots(figsize=pltsize)
ax.margins(0.02, 0.02)

rects1 = ax.bar(index - 3*width/2, helib, width,
                color='xkcd:light salmon', hatch='//', edgecolor='black', linewidth=1)
rects2 = ax.bar(index - width/2, lattigo, width,
                color='#ffddbf', hatch='xx', edgecolor='black', linewidth=1)
rects3 = ax.bar(index + width/2, palisade, width,
                color='xkcd:ecru', hatch='..', edgecolor='black', linewidth=1)
rects4 = ax.bar(index + 3*width/2, seal, width,
                color='xkcd:very light green', hatch='--', edgecolor='black', linewidth=1)
rects5 = ax.bar(index + 5*width/2, tfhe, width,
                color='xkcd:very light blue', hatch='\\\\', edgecolor='black', linewidth=1)

ax.set_axisbelow(True)
ax.grid(True, axis='y', which="both", linewidth = "0.3", linestyle='--')
ax.set_yscale('log')
ax.set_ylim([1, 60000])
ax.set_yticks([1, 10, 100, 1000, 10000])
ax.set_ylabel("Time (sec.)")
# ax.set_xlabel("Binary Domain")
ax.set_xticks(index + width / 2)
ax.set_xticklabels(x_axis_label)
ax.legend((rects1[0], rects2[0], rects3[0], rects4[0], rects5[0]),
          ("HElib", "Lattigo", "PALIS.", "SEAL", "TFHE"),
          fontsize=8, ncol=1, loc='center right', bbox_to_anchor=(1.21, 0.5))

def autolabel_above(rects):
  for rect in rects:
    height = rect.get_height()
    if height*1000 == data['$|v| = 4$']['lattigo'] or height*1000 == data['$|v| = 8$']['lattigo'] or height*1000 == data['$|v| = 16$']['lattigo']:
      ax.text(rect.get_x() + rect.get_width()/2., 5, 'Noisy', color='black', bbox=dict(facecolor='none', color='#ffddbf', linewidth=4, boxstyle='square'), ha='center', va='bottom', fontsize=8, rotation=90)
    elif height*1000 == data['$|v| = 4$']['palisade'] or height*1000 == data['$|v| = 8$']['palisade'] or height*1000 == data['$|v| = 16$']['palisade']:
      ax.text(rect.get_x() + rect.get_width()/2., 5, 'Noisy', color='black', bbox=dict(facecolor='none', color='xkcd:ecru', linewidth=4, boxstyle='square'), ha='center', va='bottom', fontsize=8, rotation=90)
    elif height*1000 == data['$|v| = 4$']['seal'] or height*1000 == data['$|v| = 8$']['seal'] or height*1000 == data['$|v| = 16$']['seal']:
      ax.text(rect.get_x() + rect.get_width()/2., 5, 'Noisy', color='black', bbox=dict(facecolor='none', color='xkcd:very light green', linewidth=4, boxstyle='square'), ha='center', va='bottom', fontsize=8, rotation=90)
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
plt.savefig("./isort.png", dpi=mydpi, bbox_inches="tight", pad_inches=0.03)
