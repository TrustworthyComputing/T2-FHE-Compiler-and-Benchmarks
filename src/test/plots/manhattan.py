#!/usr/bin/python

import matplotlib.pyplot as plt
import numpy as np

mydpi = 300
pltsize = (6, 1.5)

# Milliseconds
data = {
'Integer $|v| = 4$' : {
  'helib': 7440,
  'lattigo': 101,
  'palisade': 102,
  'seal': 41174,
  'tfhe': 5533 },
'Binary $|v| = 4$' : {
  'helib': 27821,
  'lattigo': 300617.807603,
  'palisade': 96382,
  'seal': 103,
  'tfhe': 3618 },
'Integer $|v| = 8$' : {
  'helib': 14882,
  'lattigo': 101,
  'palisade': 102,
  'seal': 82346,
  'tfhe': 11073 },
'Binary $|v| = 8$' : {
  'helib': 51405,
  'lattigo': 805777.32338,
  'palisade': 190134,
  'seal': 103,
  'tfhe': 7259 },
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
                color='xkcd:ecru', hatch='--', edgecolor='black', linewidth=1)
rects4 = ax.bar(index + 3*width/2, seal, width,
                color='xkcd:very light green', hatch='..', edgecolor='black', linewidth=1)
rects5 = ax.bar(index + 5*width/2, tfhe, width,
                color='xkcd:very light blue', hatch='\\\\', edgecolor='black', linewidth=1)

ax.set_axisbelow(True)
ax.grid(True, axis='y', which="both", linewidth = "0.3", linestyle='--')

ax.set_yscale('log')
ax.set_ylim([1, 3000])
ax.set_yticks([1, 10, 100, 1000])
ax.set_ylabel("Time (sec.)")
# ax.set_xlabel("Encrypted Domain")
ax.set_xticks(index + width / 2)
ax.set_xticklabels(x_axis_label)
# ax.legend((rects1[0], rects2[0], rects3[0], rects4[0], rects5[0]),
#           ("HElib", "Lattigo", "PALISADE", "SEAL", "TFHE"),
#           fontsize=9, ncol=3, loc='upper left')

def autolabel_above(rects):
  for rect in rects:
    height = rect.get_height()
    if height <= 3.0:
      if height*1000 == data['Integer $|v| = 4$']['lattigo'] or height*1000 == data['Integer $|v| = 8$']['lattigo']:
        ax.text(rect.get_x() + rect.get_width()/2., 5, 'Noisy', color='black', bbox=dict(facecolor='none', color='#ffddbf', linewidth=1, boxstyle='square'), ha='center', va='bottom', fontsize=8, rotation=90)
      elif height*1000 == data['Integer $|v| = 4$']['palisade'] or height*1000 == data['Integer $|v| = 8$']['palisade']:
        ax.text(rect.get_x() + rect.get_width()/2., 5, 'Noisy', color='black', bbox=dict(facecolor='none', color='xkcd:ecru', linewidth=1, boxstyle='square'), ha='center', va='bottom', fontsize=8, rotation=90)
      elif height*1000 == data['Binary $|v| = 4$']['seal'] or height*1000 == data['Binary $|v| = 8$']['seal']:
        ax.text(rect.get_x() + rect.get_width()/2., 5, 'Noisy', color='black', bbox=dict(facecolor='none', color='xkcd:very light green', linewidth=1, boxstyle='square'), ha='center', va='bottom', fontsize=8, rotation=90)

      # ax.text(rect.get_x() + rect.get_width()/2., 1.5*height, 'Noisy', ha='center', va='bottom', fontsize=7, rotation=90)
    elif height < 10:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.2f' % (height), ha='center', va='bottom', fontsize=7)
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
plt.savefig("./manhattan.png", dpi=mydpi, bbox_inches="tight", pad_inches=0.03)
