import React from 'react';

const NeonButton = ({ text, onClick, variant = 'primary', disabled = false, className = '' }) => {
  
  // Configuration des styles selon la variante
  const styles = {
    primary: "bg-neon-cyan text-black shadow-[0_0_20px_rgba(0,240,255,0.4)] hover:shadow-[0_0_35px_rgba(0,240,255,0.7)] border-neon-cyan",
    danger: "bg-neon-magenta text-white shadow-[0_0_20px_rgba(255,0,85,0.4)] hover:shadow-[0_0_35px_rgba(255,0,85,0.7)] border-neon-magenta",
    ghost: "bg-transparent text-white border-white/30 border hover:bg-white/10 hover:border-white",
  };

  // Style de base (forme, police, transition)
  const baseStyle = "px-8 py-4 rounded-full font-tech font-bold text-xl uppercase tracking-wider transition-all duration-300 transform hover:scale-105 active:scale-95 border-2";

  // Gestion de l'état désactivé
  const finalStyle = disabled 
    ? "bg-gray-800 text-gray-500 border-gray-800 cursor-not-allowed px-8 py-4 rounded-full font-tech font-bold text-xl uppercase" 
    : `${baseStyle} ${styles[variant]} ${className}`;

  return (
    <button 
      onClick={disabled ? null : onClick} 
      className={finalStyle}
    >
      {text}
    </button>
  );
};

export default NeonButton;