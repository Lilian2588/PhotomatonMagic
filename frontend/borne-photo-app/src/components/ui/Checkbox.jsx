import React from 'react';
import { Check } from 'lucide-react';

const CustomCheckbox = ({ checked, onChange, label }) => {
  return (
    <div 
      className="flex items-center gap-3 cursor-pointer group"
      onClick={() => onChange(!checked)}
    >
      {/* La boîte de la checkbox */}
      <div className={`
        w-6 h-6 rounded border-2 flex items-center justify-center transition-all duration-300
        ${checked 
          ? 'bg-neon-cyan border-neon-cyan shadow-[0_0_10px_rgba(0,240,255,0.5)]' 
          : 'bg-transparent border-gray-600 group-hover:border-white'}
      `}>
        {/* L'icône (visible seulement si coché) */}
        {checked && <Check size={16} className="text-black font-bold" />}
      </div>

      {/* Le texte du label */}
      <span className={`text-sm select-none ${checked ? 'text-white' : 'text-gray-400'}`}>
        {label}
      </span>
    </div>
  );
};

export default CustomCheckbox;