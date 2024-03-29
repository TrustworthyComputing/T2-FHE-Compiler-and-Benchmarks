package functional_units

import (
	"fmt"
	"github.com/tuneinsight/lattigo/v4/bfv"
	"github.com/tuneinsight/lattigo/v4/rlwe"
	"math"
)

var encryptorPk_ *rlwe.Encryptor
var encoder_ *bfv.Encoder
var evaluator_ *bfv.Evaluator
var params_ *bfv.Parameters
var ptxt_mod_ int
var word_sz_ int
var slots_ int

func FunitsInit(encryptorPk *rlwe.Encryptor, encoder *bfv.Encoder,
	evaluator *bfv.Evaluator, params *bfv.Parameters,
	ptxt_mod, slots, word_sz int) {
	encryptorPk_ = encryptorPk
	word_sz_ = word_sz
	encoder_ = encoder
	evaluator_ = evaluator
	params_ = params
	ptxt_mod_ = ptxt_mod
	slots_ = slots
}

func EqPlain(ctxt *rlwe.Ciphertext, ptxt *rlwe.Plaintext) *rlwe.Ciphertext {
	ctxt2 := (*encryptorPk_).EncryptNew(ptxt)
	return Eq(ctxt, ctxt2)
}

func Eq(c1, c2 *rlwe.Ciphertext) *rlwe.Ciphertext {
	num_squares := int(math.Log2(float64(ptxt_mod_ - 1)))
	tmp_ := c1.CopyNew()
	(*evaluator_).Sub(tmp_, c2, tmp_)
	tmp2_ := tmp_.CopyNew()
	for i := 0; i < num_squares; i++ { // square
		receiver := bfv.NewCiphertext(*params_, tmp2_.Degree()*2, (*params_).MaxLevel())
		(*evaluator_).Mul(tmp2_, tmp2_, receiver)
		tmp2_ = (*evaluator_).RelinearizeNew(receiver)
	}
	remaining := int((ptxt_mod_ - 1) - int(math.Pow(2.0, float64(num_squares))))
	for i := 0; i < remaining; i++ { // mult
		receiver := bfv.NewCiphertext(*params_, tmp2_.Degree()+tmp_.Degree(), (*params_).MaxLevel())
		(*evaluator_).Mul(tmp2_, tmp_, receiver)
		tmp2_ = (*evaluator_).RelinearizeNew(receiver)
	}
	(*evaluator_).Neg(tmp2_, tmp2_)
	ptxt := bfv.NewPlaintext(*params_, (*params_).MaxLevel())
	ones := make([]uint64, slots_)
	for i := 0; i < slots_; i++ {
		ones[i] = 1
	}
	(*encoder_).Encode(ones, ptxt)
	ones_ := (*encryptorPk_).EncryptNew(ptxt)
	(*evaluator_).Add(tmp2_, ones_, tmp2_)
	return tmp2_
}

func NeqPlain(ctxt *rlwe.Ciphertext, ptxt *rlwe.Plaintext) *rlwe.Ciphertext {
	ctxt2 := (*encryptorPk_).EncryptNew(ptxt)
	return Neq(ctxt, ctxt2)
}

func Neq(c1, c2 *rlwe.Ciphertext) *rlwe.Ciphertext {
	res_ := Eq(c1, c2)
	(*evaluator_).Neg(res_, res_)
	ptxt := bfv.NewPlaintext(*params_, (*params_).MaxLevel())
	ones := make([]uint64, slots_)
	for i := 0; i < slots_; i++ {
		ones[i] = 1
	}
	(*encoder_).Encode(ones, ptxt)
	ones_ := (*encryptorPk_).EncryptNew(ptxt)
	(*evaluator_).Add(res_, ones_, res_)
	return res_;
}

func LtPlainRight(ctxt *rlwe.Ciphertext, ptxt *rlwe.Plaintext) *rlwe.Ciphertext {
	ctxt2 := (*encryptorPk_).EncryptNew(ptxt)
	return Lt(ctxt, ctxt2)
}

func LtPlainLeft(ptxt *rlwe.Plaintext, ctxt *rlwe.Ciphertext) *rlwe.Ciphertext {
	ctxt1 := (*encryptorPk_).EncryptNew(ptxt)
	return Lt(ctxt1, ctxt)
}

func Lt(c1, c2 *rlwe.Ciphertext) *rlwe.Ciphertext {
	num_squares := int(math.Log2(float64(ptxt_mod_ - 1)))
	ptxt := bfv.NewPlaintext(*params_, (*params_).MaxLevel())
	arr := make([]uint64, slots_)
	for i := 0; i < slots_; i++ {
		arr[i] = 0
	}
	(*encoder_).Encode(arr, ptxt)
	result_ := (*encryptorPk_).EncryptNew(ptxt)
	start := -(ptxt_mod_ - 1) / 2
	for i := start; i < 0; i++ { // square
		tmp_ := c1.CopyNew()
		(*evaluator_).Sub(tmp_, c2, tmp_)
		for j := 0; j < slots_; j++ {
			arr[j] = uint64(-i)
		}
		(*encoder_).Encode(arr, ptxt)
		i_ := (*encryptorPk_).EncryptNew(ptxt)
		(*evaluator_).Add(tmp_, i_, tmp_)
		tmp2_ := tmp_.CopyNew()
		for j := 0; j < num_squares; j++ { // square
			receiver := bfv.NewCiphertext(*params_, tmp2_.Degree()*2, (*params_).MaxLevel())
			(*evaluator_).Mul(tmp2_, tmp2_, receiver)
			tmp2_ = (*evaluator_).RelinearizeNew(receiver)
		}
		remaining := int((ptxt_mod_ - 1) - int(math.Pow(2.0, float64(num_squares))))
		for j := 0; j < remaining; j++ { // mult
			receiver := bfv.NewCiphertext(*params_, tmp2_.Degree()+tmp_.Degree(), (*params_).MaxLevel())
			(*evaluator_).Mul(tmp2_, tmp_, receiver)
			tmp2_ = (*evaluator_).RelinearizeNew(receiver)
		}
		tmp_ = tmp2_.CopyNew()
		(*evaluator_).Neg(tmp_, tmp_)
		for j := 0; j < slots_; j++ {
			arr[j] = 1
		}
		(*encoder_).Encode(arr, ptxt)
		ones_ := (*encryptorPk_).EncryptNew(ptxt)
		(*evaluator_).Add(tmp_, ones_, tmp_)
		(*evaluator_).Add(result_, tmp_, result_)
	}
	return result_
}

func LeqPlainRight(ctxt *rlwe.Ciphertext, ptxt *rlwe.Plaintext) *rlwe.Ciphertext {
	ctxt2 := (*encryptorPk_).EncryptNew(ptxt)
	return Leq(ctxt, ctxt2)
}

func LeqPlainLeft(ptxt *rlwe.Plaintext, ctxt *rlwe.Ciphertext) *rlwe.Ciphertext {
	ctxt1 := (*encryptorPk_).EncryptNew(ptxt)
	return Leq(ctxt1, ctxt)
}

func Leq(c1, c2 *rlwe.Ciphertext) *rlwe.Ciphertext {
	less_ := Lt(c1, c2)
	equal_ := Eq(c1, c2)
	tmp_ := less_.CopyNew()
	receiver := bfv.NewCiphertext(*params_, tmp_.Degree()+equal_.Degree(), (*params_).MaxLevel())
	(*evaluator_).Mul(tmp_, equal_, receiver)
	tmp_ = (*evaluator_).RelinearizeNew(receiver)
	result_ := less_.CopyNew()
	(*evaluator_).Add(result_, equal_, result_)
	(*evaluator_).Sub(result_, tmp_, result_)
	return result_
}

func BinEq(c1, c2 []*rlwe.Ciphertext, in_len int) []*rlwe.Ciphertext {
	res := make([]*rlwe.Ciphertext, in_len)
	tmp_ := bfv.NewCiphertext(*params_, c1[0].Degree(), (*params_).MaxLevel())
	receiver := bfv.NewCiphertext(*params_, tmp_.Degree()*2, (*params_).MaxLevel())
	one := bfv.NewPlaintext(*params_, (*params_).MaxLevel())
	arr := make([]uint64, slots_)
	for i := 0; i < slots_; i++ {
		arr[i] = 1
	}
	(*encoder_).Encode(arr, one)
	zero := bfv.NewPlaintext(*params_, (*params_).MaxLevel())
	for i := 0; i < slots_; i++ {
		arr[i] = 0
	}
	(*encoder_).Encode(arr, zero)
	for i := in_len-1; i >= 0; i-- {
		tmp_res_ := (*encryptorPk_).EncryptNew(one)
		(*evaluator_).Sub(c1[i], c2[i], tmp_)
		(*evaluator_).Mul(tmp_, tmp_, receiver)
		tmp_ = (*evaluator_).RelinearizeNew(receiver)
		(*evaluator_).Sub(tmp_res_, tmp_, tmp_res_)
		if i == (in_len-1) {
			res[in_len-1] = tmp_res_.CopyNew()
		} else {
			(*evaluator_).Mul(res[in_len-1], tmp_res_, receiver)
			res[in_len-1] = (*evaluator_).RelinearizeNew(receiver)
			res[i] = (*encryptorPk_).EncryptNew(zero)
		}
	}
	return res
}

func BinNeq(c1, c2 []*rlwe.Ciphertext, in_len int) []*rlwe.Ciphertext {
	res := BinEq(c1, c2, in_len)
	(*evaluator_).Neg(res[in_len-1], res[in_len-1])
	ptxt := bfv.NewPlaintext(*params_, (*params_).MaxLevel())
	ones := make([]uint64, slots_)
	for i := 0; i < slots_; i++ {
		ones[i] = 1
	}
	(*encoder_).Encode(ones, ptxt)
	ones_ := (*encryptorPk_).EncryptNew(ptxt)
	(*evaluator_).Add(res[in_len-1], ones_, res[in_len-1])
	return res;
}

func EncodeAllSlots(val uint64) *rlwe.Plaintext {
	ptxt := bfv.NewPlaintext(*params_, (*params_).MaxLevel())
	arr := make([]uint64, slots_)
	for i := 0; i < slots_; i++ {
		arr[i] = val
	}
	(*encoder_).Encode(arr, ptxt)
	return ptxt
}

func slice(in_ []*rlwe.Ciphertext, start, end int) []*rlwe.Ciphertext {
	res := make([]*rlwe.Ciphertext, end-start)
	for i := start; i < end; i++ {
		res[i-start] = in_[i].CopyNew()
	}
	return res
}

func BinSingleXor(c1, c2 *rlwe.Ciphertext) *rlwe.Ciphertext {
	res := (*evaluator_).SubNew(c1, c2)
	receiver := (*evaluator_).MulNew(res, res)
	(*evaluator_).Relinearize(receiver, res)
	return res
}

func Mux(sel, c1, c2 *rlwe.Ciphertext) *rlwe.Ciphertext {
	not_sel := (*evaluator_).NegNew(sel)
	one := EncodeAllSlots(1)
	(*evaluator_).Add(not_sel, one, not_sel)
	receiver := (*evaluator_).MulNew(c1, sel)
	res := (*evaluator_).RelinearizeNew(receiver)
	receiver = (*evaluator_).MulNew(c2, not_sel)
	tmp := (*evaluator_).RelinearizeNew(receiver)
	(*evaluator_).Add(res, tmp, res)
	return res
}

func BinShiftRightLogical(ct []*rlwe.Ciphertext, amt int64) []*rlwe.Ciphertext {
	if amt < int64(len(ct)) - 1 {
		fmt.Errorf("BinShiftRight: shift ammount too big")
	}
	res := make([]*rlwe.Ciphertext, len(ct))
	zero := EncodeAllSlots(0)
	// shift data (MSB is at 0, LSB is at size - 1)
	for i := int64(len(ct)) - amt - 1; i >= 0; i-- {
		res[i + amt] = ct[i]
	}
	// shift in zeros
	for i := amt - 1; i >= 0; i-- {
		res[i] = (*encryptorPk_).EncryptNew(zero)
	}
	return res
}

func BinShiftRight(ct []*rlwe.Ciphertext, amt int64) []*rlwe.Ciphertext {
	if amt < int64(len(ct)) - 1 {
		fmt.Errorf("BinShiftRight: shift ammount too big")
	}
	res := make([]*rlwe.Ciphertext, len(ct))
	// shift data (MSB is at 0, LSB is at size - 1)
	for i := int64(len(ct)) - amt - 1; i >= 0; i-- {
		res[i + amt] = ct[i]
	}
	// copy sign
	for i := amt - 1; i >= 0; i-- {
		res[i] = ct[0]
	}
	return res
}

func BinShiftLeft(ct []*rlwe.Ciphertext, amt int64) []*rlwe.Ciphertext {
	if amt < int64(len(ct)) - 1 {
		fmt.Errorf("BinShiftRight: shift ammount too big")
	}
	res := make([]*rlwe.Ciphertext, len(ct))
	// Initialize with zeros
	zero := EncodeAllSlots(0)
	for i := 0; i < len(res); i++ {
		res[i] = (*encryptorPk_).EncryptNew(zero)
	}
	// shift data (MSB is at 0, LSB is at size - 1)
	for i := amt; i < int64(len(ct)); i++ {
		res[i - amt] = ct[i];
	}
	return res
}

func BinMux(sel, c1, c2 []*rlwe.Ciphertext) []*rlwe.Ciphertext {
	res := make([]*rlwe.Ciphertext, len(c1))
	not_sel := (*evaluator_).NegNew(sel[len(sel)-1])
	one := EncodeAllSlots(1)
	(*evaluator_).Add(not_sel, one, not_sel)
	for i := 0; i < len(res); i++ {
		receiver := (*evaluator_).MulNew(c1[i], sel[len(sel)-1])
		res[i] = (*evaluator_).RelinearizeNew(receiver)
		receiver = (*evaluator_).MulNew(c2[i], not_sel)
		tmp := (*evaluator_).RelinearizeNew(receiver)
		(*evaluator_).Add(res[i], tmp, res[i])	
	}
	return res
}

func BinXor(c1, c2 []*rlwe.Ciphertext) []*rlwe.Ciphertext {
	res := make([]*rlwe.Ciphertext, len(c1))
	if ptxt_mod_ > 2 {
		for i := 0; i < len(res); i++ {
			res[i] = (*evaluator_).SubNew(c1[i], c2[i])
			receiver := (*evaluator_).MulNew(res[i], res[i])
			res[i] = (*evaluator_).RelinearizeNew(receiver)
		}
	} else {
		for i := 0; i < len(res); i++ {
			res[i] = (*evaluator_).AddNew(c1[i], c2[i])
		}
	}
	return res
}

func BinOr(c1, c2 []*rlwe.Ciphertext) []*rlwe.Ciphertext {
	res := make([]*rlwe.Ciphertext, len(c1))
	for i := 0; i < len(res); i++ {
		receiver := (*evaluator_).MulNew(c1[i], c2[i])
		tmp_ctxt := (*evaluator_).RelinearizeNew(receiver)
		tmp_ctxt_2 := (*evaluator_).AddNew(c1[i], c2[i])
		res[i] = (*evaluator_).SubNew(tmp_ctxt_2, tmp_ctxt)
	}
	return res
}

func BinAnd(c1, c2 []*rlwe.Ciphertext) []*rlwe.Ciphertext {
	res := make([]*rlwe.Ciphertext, len(c1))
	for i := 0; i < len(res); i++ {
		receiver := (*evaluator_).MulNew(c1[i], c2[i])
		res[i] = (*evaluator_).RelinearizeNew(receiver)
	}
	return res
}

func BinLt(c1, c2 []*rlwe.Ciphertext) []*rlwe.Ciphertext {
	res := make([]*rlwe.Ciphertext, word_sz_)
	if len(c1) == 1 {
		one := EncodeAllSlots(1)
		c1_neg := (*evaluator_).NegNew(c1[0])
		(*evaluator_).Add(c1_neg, one, c1_neg)
		receiver := (*evaluator_).MulNew(c1_neg, c2[0])
		res[word_sz_-1] = (*evaluator_).RelinearizeNew(receiver)
		return res
	}
	length := len(c1) >> 1
	lhs_h := slice(c1, 0, length)
	lhs_l := slice(c1, length, len(c1))
	rhs_h := slice(c2, 0, length)
	rhs_l := slice(c2, length, len(c2))
	term1 := BinLt(lhs_h, rhs_h)
	h_equal := BinEq(lhs_h, rhs_h, len(lhs_h))
	l_equal := BinLt(lhs_l, rhs_l)
	receiver := (*evaluator_).MulNew(h_equal[len(lhs_h)-1], l_equal[word_sz_-1])
	term2 := (*evaluator_).RelinearizeNew(receiver)
	res[word_sz_-1] = BinSingleXor(term1[word_sz_-1], term2)
	zero := EncodeAllSlots(0)
	for i := 0; i < (word_sz_-1); i++ {
		res[i] = (*encryptorPk_).EncryptNew(zero)
	}
	return res
}

func BinLeq(c1, c2 []*rlwe.Ciphertext) []*rlwe.Ciphertext {
	res := BinLt(c2, c1)
	one := EncodeAllSlots(1)
	(*evaluator_).Neg(res[word_sz_ - 1], res[word_sz_ - 1])
	(*evaluator_).Add(res[word_sz_-1], one, res[word_sz_-1])
	return res
}

func BinInc(c1 []*rlwe.Ciphertext) []*rlwe.Ciphertext {
	res := make([]*rlwe.Ciphertext, len(c1))
	carry_ptxt := EncodeAllSlots(1)
	carry := (*encryptorPk_).EncryptNew(carry_ptxt)
	for i := (len(c1)-1); i > 0; i-- {
		res[i] = BinSingleXor(c1[i], carry)
		receiver := (*evaluator_).MulNew(c1[i], carry)
		carry = (*evaluator_).RelinearizeNew(receiver)
	}
	res[0] = BinSingleXor(c1[0], carry)
	return res
}

func BinDec(c1 []*rlwe.Ciphertext) []*rlwe.Ciphertext {
	res := make([]*rlwe.Ciphertext, len(c1))
	carry_ptxt := EncodeAllSlots(1)
	carry := (*encryptorPk_).EncryptNew(carry_ptxt)
	for i := (len(c1)-1); i > 0; i-- {
		res[i] = BinSingleXor(c1[i], carry)
		neg_ct1 := (*evaluator_).NegNew(c1[i])
		(*evaluator_).Add(neg_ct1, carry_ptxt, neg_ct1)
		receiver := (*evaluator_).MulNew(neg_ct1, carry)
		carry = (*evaluator_).RelinearizeNew(receiver)
	}
	res[0] = BinSingleXor(c1[0], carry)
	return res
}

func BinAdd(c1, c2 []*rlwe.Ciphertext) []*rlwe.Ciphertext {
	carry_ptxt := EncodeAllSlots(0)
	carry := (*encryptorPk_).EncryptNew(carry_ptxt)
	smaller := c2
	bigger := c1
	if len(c1) < len(c2) {
		smaller = c1
		bigger = c2
	}
	offset := len(bigger) - len(smaller)
	res := make([]*rlwe.Ciphertext, len(smaller))
	for i := len(smaller) - 1; i >= 0; i-- {
		xor_ := BinSingleXor(smaller[i], bigger[i + offset])
		res[i] = BinSingleXor(xor_, carry)
		if i == 0 {
			break
		}
		receiver := (*evaluator_).MulNew(smaller[i], bigger[i+offset])
		prod_ := (*evaluator_).RelinearizeNew(receiver)
		(*evaluator_).Mul(carry, xor_, receiver)
		xor_ = (*evaluator_).RelinearizeNew(receiver)
		carry = BinSingleXor(prod_, xor_)
	}
	return res
}

func BinSub(c1, c2 []*rlwe.Ciphertext) []*rlwe.Ciphertext {
	if len(c1) != len(c2) {
		fmt.Errorf("BinSub: bitsize is not equal")
	}
	carry_ptxt := EncodeAllSlots(0)
	one := EncodeAllSlots(1)
	carry := (*encryptorPk_).EncryptNew(carry_ptxt)
	res := make([]*rlwe.Ciphertext, len(c1))
	neg_c2 := make([]*rlwe.Ciphertext, len(c2))
	for i := len(c2) - 1; i >= 0; i-- {
		neg_c2[i] = (*evaluator_).NegNew(c2[i])
		(*evaluator_).Add(neg_c2[i], one, neg_c2[i])
	}
	neg_c2 = BinInc(neg_c2)
	for i := len(c1) - 1; i >= 0; i-- {
		xor_ := BinSingleXor(c1[i], neg_c2[i])
		res[i] = BinSingleXor(xor_, carry)
		if i == 0 {
			break
		}
		receiver := (*evaluator_).MulNew(c1[i], neg_c2[i])
		prod_ := (*evaluator_).RelinearizeNew(receiver)
		(*evaluator_).Mul(carry, xor_, receiver)
		xor_ = (*evaluator_).RelinearizeNew(receiver)
		carry = BinSingleXor(prod_, xor_)
	}
	return res
}

func BinMult(c1, c2 []*rlwe.Ciphertext) []*rlwe.Ciphertext {
	if len(c1) != len(c2) {
		fmt.Errorf("BinMult: bitsize is not equal")
	}
	ctlen := len(c1)
	tmp := make([]*rlwe.Ciphertext, ctlen)
	prod := make([]*rlwe.Ciphertext, ctlen)
	zero := EncodeAllSlots(0)
	for i := 0; i < ctlen; i++ {
		prod[i] = (*encryptorPk_).EncryptNew(zero)
	}
	for i := ctlen - 1; i >= 0; i-- {
		for j := ctlen - 1; j >= ctlen - i - 1; j-- {
			receiver := (*evaluator_).MulNew(c1[i], c2[j])
			tmp[j] = (*evaluator_).RelinearizeNew(receiver)
		}
		tmp_slice := slice(prod, 0, i+1)
		tmp_slice = BinAdd(tmp_slice, tmp)
		for j := i; j >= 0; j-- {
			prod[j] = tmp_slice[j]
		}
	}
	return prod
}

func BinNot(ct []*rlwe.Ciphertext) []*rlwe.Ciphertext {
	res := make([]*rlwe.Ciphertext, len(ct))
	one := EncodeAllSlots(1)
	for i := 0; i < len(res); i++ {
		res[i] = (*evaluator_).NegNew(ct[i])
		(*evaluator_).Add(res[i], one, res[i]);
	}
	return res
}

func Round(val float64, precision int) float64 {
	return math.Round(val*(math.Pow10(precision))) / math.Pow10(precision)
}
