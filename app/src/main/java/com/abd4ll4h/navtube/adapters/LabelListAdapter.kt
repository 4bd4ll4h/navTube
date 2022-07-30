package com.abd4ll4h.navtube.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abd4ll4h.navtube.dataBase.tables.Label
import com.abd4ll4h.navtube.databinding.LabelShipBinding
import com.google.android.material.chip.Chip

class LabelListAdapter(
    val itemClicked: ItemClick,
    var labelList: List<Label>,
    var checkedLabelList: ArrayList<Int> = arrayListOf()
) :
    RecyclerView.Adapter<LabelListAdapter.ViewHolder>() {
    var lastCheckedChip:Chip?=null

    inner class ViewHolder(val binding: LabelShipBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.chip.setOnClickListener {
                if (lastCheckedChip!=null && lastCheckedChip !=it){
                    lastCheckedChip!!.isChecked=false
                }
                if ((it as Chip).isChecked){
                    checkedLabelList.clear()
                    checkedLabelList.add(labelList[adapterPosition].id)
                }else checkedLabelList.remove(labelList[adapterPosition].id)
                lastCheckedChip=it
                labelList[adapterPosition].isChecked= it.isChecked
                itemClicked.onClick(labelList[adapterPosition],it.isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LabelShipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.chip.text = labelList[position].label
        holder.binding.chip.isChecked= checkedLabelList.isNotEmpty() && labelList[position].id in checkedLabelList

    }

    interface ItemClick {
        fun onClick(label: Label, checked: Boolean)
    }



    override fun getItemCount(): Int {
        return labelList.size
    }

    fun submitList(list: List<Label>, checkedLabelList: ArrayList<Int>) {
        labelList=list
        this.checkedLabelList=checkedLabelList
       notifyDataSetChanged()
    }
}